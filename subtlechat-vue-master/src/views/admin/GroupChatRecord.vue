<template>
  <div>
    <div>
      Sender Nickname: <el-input v-model="nameKeyword" placeholder="Enter sender nickname" class="name-input"></el-input>
      Send Time:
      <el-date-picker
          v-model="dateScope"
          type="datetimerange"
          range-separator="to"
          start-placeholder="Start date"
          end-placeholder="End date"
          :editable="false"
          :unlink-panels="true"
          value-format="yyyy-MM-dd HH:mm:ss"
          class="top-controls-bar">
      </el-date-picker>
      Message Type:
      <el-radio-group v-model="msgTypeRadio" class="top-controls-bar">
        <el-radio :label="1">Text</el-radio>
        <el-radio :label="2">Image</el-radio>
        <el-radio :label="3">File</el-radio>
      </el-radio-group>
      <el-button @click="initMessTableData" icon="el-icon-search" type="primary" size="small" class="top-controls-bar">Search</el-button>
      <el-button @click="refreshMessTableData" icon="el-icon-refresh" type="primary" size="small" class="top-controls-bar">Refresh Table</el-button>
      <el-button @click="exportData" icon="el-icon-document" type="success" size="small" class="top-controls-bar">Export to Excel</el-button>
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
            label="Send Time"
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
                        style="width: 50px;height: 50px">
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
      <el-button @click="handleMultiDelete" :disabled="multipleSelection.length==0?true:false" type="danger" icon="el-icon-delete">Delete in Batch</el-button>
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
        multipleSelection:[],//选中的每一项
        page:1,//起始页数
        size:10,//单页显示数据数
        total:-1,//表格数据总数
        nameKeyword:'',//查询昵称关键字
        dateScope:null,//日期时间范围数组
        loading:false,
      }
    },
    mounted(){
      this.initMessTableData();
    },
    methods:{
      //初始化表格数据
      initMessTableData(){
        this.loading=true;
        let url="/groupMsgContent/page?page="+this.page+"&size="+this.size;
        if (this.nameKeyword!=''){
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
      //刷新表格
      refreshMessTableData(){
        this.nameKeyword='';
        this.dateScope=null;
        this.msgTypeRadio=-1;
        this.initMessTableData();
      },
      //导出数据到Excel文件中
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
      //当选择项发生变化时会触发该事件
      handleSelectionChange(val){
        this.multipleSelection=val;
      },
      //单条数据删除
      handleDelete(data){
        this.$confirm('This operation will permanently delete the message record. Do you want to continue?', 'Tips', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          //点击确定后即执行
          this.deleteRequest("/groupMsgContent/"+data.id).then(resp=>{
            if (resp){
              this.initMessTableData();
            }
          })
        }).catch(() => {
          //点击了取消
          this.$message({
            type: 'info',
            message: 'undeleted'
          });
        });
      },
      //批量删除数据
      handleMultiDelete(){
        this.$confirm('This operation will be permanently deleted【'+this.multipleSelection.length+'】 records, whether to continue?', 'Tips', {
          confirmButtonText: 'Confirm',
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
              message: 'undeleted'
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
