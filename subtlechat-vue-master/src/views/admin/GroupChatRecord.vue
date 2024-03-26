<template>
  <div>
    <div>
      Sender Nickname: <el-input v-model="nameKeyword" placeholder="Enter sender nickname" class="nameInput"></el-input>
      Sending Time:
      <el-date-picker
            v-model="dateScope"
            type="datetimerange"
            range-separator="to"
            start-placeholder="Start Date"
            end-placeholder="End Date"
            :editable="false"
            :unlink-panels="true"
            value-format="yyyy-MM-dd HH:mm:ss"
            class="topControlsBar">
    </el-date-picker>
      Message Type:
      <el-radio-group v-model="msgTypeRadio" class="topControlsBar">
        <el-radio :label="1">Text</el-radio>
        <el-radio :label="2">Image</el-radio>
        <el-radio :label="3">File</el-radio>
      </el-radio-group>
      <el-button @click="initMessTableData" icon="el-icon-search" type="primary" size="small" class="topControlsBar">Search</el-button>
      <el-button @click="refreshMessTableData" icon="el-icon-refresh" type="primary" size="small" class="topControlsBar">Refresh Table</el-button>
      
    </div>
    <div style="margin-top: 15px">
      <el-table
              :data="messTableData"
              v-loading="loading"
              border
              stripe
              @selection-change="handleSelectionChange"
              style="width: 100%">
        <el-table-column
                type="selection"
                width="55">
        </el-table-column>
        <el-table-column
                prop="id"
                label="Message ID"
                width="80">
        </el-table-column>
        <el-table-column
                prop="fromId"
                label="Sender ID"
                width="100">
        </el-table-column>
        <el-table-column
                prop="fromName"
                label="Sender Nickname"
                width="150">
        </el-table-column>
        <el-table-column
                prop="createTime"
                label="Sending Time"
                width="180">
        </el-table-column>
        <el-table-column
                label="Content"
                width="750">
          <template slot-scope="scope">
            <div v-if="scope.row.messageTypeId==1" v-html="scope.row.content"></div>
            <div v-else>
              <el-image :src="scope.row.content"
                        :preview-src-list="[scope.row.content]"
                        style="width: 50px; height: 50px">
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Action">
          <template slot-scope="scope">
            <el-button
                    size="mini"
                    type="danger"
                    @click="handleDelete(scope.row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>
    <div style="display: flex; justify-content: space-between; margin-top: 10px">
      <el-button @click="handleMultiDelete" :disabled="multipleSelection.length==0?true:false" type="danger" icon="el-icon-delete">Bulk Delete</el-button>
      <el-pagination
              @size-change="handleSizeChange"
              @current-change="handleCurrentChange"
              :page-sizes="[10, 20, 30, 40]"
              :page-size="10"
              layout="total, sizes, prev, pager, next, jumper"
              :total="total"
              style="display: inline-block">
      </el-pagination>
      </div>
  </div>
</template>

<script>
  export default {
    name: "GroupChatRecord",
    data(){
      return{
        messTableData: [],
        msgTypeRadio:-1,
        multipleSelection:[], // Selected items
        page:1, // Starting page number
        size:10, // Number of data items displayed per page
        total:-1, // Total number of table data items
        nameKeyword:'', // Search nickname keyword
        dateScope:null, // Date time range array
        loading:false,
      }
    },
    mounted(){
      this.initMessTableData();
    },
    methods:{
      // Initialize table data
      initMessTableData(){
        this.loading=true;
        let url="/groupMsgContent/page?page="+this.page+"&size="+this.size;
        if (this.nameKeyword!=' '){
          url+="&nickname="+this.nameKeyword;
        }
        if (this.dateScope){
          url+="&dateScope="+this.dateScope;
        }
        if (this.msgTypeRadio!=-1){
          url+="&type="+this.msgTypeRadio;
        }
        this.getRequest(url).then(resp=>{
          this.messTableData=resp.data;
          this.total=resp.total;
          setTimeout(()=>{
            this.loading=false;
          },1000)
        })
      },
      // Refresh the table data
      refreshMessTableData(){
        this.nameKeyword='';
        this.dateScope=null;
        this.msgTypeRadio=-1;
        this.initMessTableData();
      },
      // Export data to an Excel file
      exportData(){
        window.open("/groupMsgContent/download","_parent");
      },
      handleSizeChange(val){
        this.size=val;
        this.initMessTableData();
      },
      handleCurrentChange(val){
        this.page=val;
        this.initMessTableData();
      },
      // Event triggered when the selection changes
      handleSelectionChange(val){
        this.multipleSelection=val;
      },
      // Delete a single data entry
      handleDelete(data){
        this.$confirm('This action will permanently delete this message record, do you want to continue?', 'Warning', {
          confirmButtonText: 'OK',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          // Action after clicking OK
          this.deleteRequest("/groupMsgContent/"+data.id).then(resp=>{
            if (resp){
              this.initMessTableData();
            }
          })
        }).catch(() => {
          // Action after clicking cancel
          this.$message({
            type: 'info',
            message: 'Deletion cancelled'
          });
        });
      },
      // Bulk delete data entries
      handleMultiDelete(){
        this.$confirm('This action will permanently delete ['+this.multipleSelection.length+'] records, do you want to continue?', 'Warning', {
          confirmButtonText: 'OK',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          let url="/groupMsgContent/?";
          this.multipleSelection.forEach(item=>{
            url+="ids="+item.id+"&";
          });
          this.deleteRequest(url).then(resp=>{
            if (resp){
              this.initMessTableData();
            }
          })
        }).catch(() => {
          this.$message({
            type: 'info',
            message: 'Deletion cancelled'
          });
        });
      }

    }
  }
</script>

<style scoped>
  .nameInput{
    width: 150px;
  }
  .topControlsBar{
    margin:0 10px;
  }
</style>
