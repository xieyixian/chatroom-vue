<template>
<div>
  <div>
    User account status selection:
    <el-select v-model="stateValue" placeholder="Select">
      <el-option
              v-for="item in stateOptions"
              :key="item.value"
              :label="item.label"
              :value="item.value">
      </el-option>
    </el-select>
    <el-input placeholder="Enter user nickname to search"
              prefix-icon="el-icon-search"
              style="width: 200px;margin: 0 10px"
               v-model="nameKeyword"></el-input>
    <el-button @click="initUserData" icon="el-icon-search" type="primary">Search</el-button>
    <el-button @click="refreshTable" icon="el-icon-refresh" type="primary">Fresh</el-button>
  </div>
  <div style="margin-top: 20px;width: 1000px">
    <el-table
            :data="userData"
            stripe
            border
            v-loading="loading"
            @selection-change="handleSelectionChange"
            style="width: 100%">
      <el-table-column
              type="selection"
              width="55">
      </el-table-column>
      <el-table-column
              prop="id"
              label="id"
              width="80">
      </el-table-column>
      <el-table-column
              prop="username"
              label="username"
              width="180">
      </el-table-column>
      <el-table-column
              prop="nickname"
              label="nickname"
              width="180">
      </el-table-column>
      <el-table-column
              label="avatar"
               width="100">
        <template slot-scope="scope">
          <el-image :src="scope.row.userProfile"
                    :preview-src-list="[scope.row.userProfile]"
                    style="width: 50px;height: 50px">
            <div slot="error" class="image-slot">
              <i class="el-icon-picture-outline"></i>
            </div>
          </el-image>
        </template>
      </el-table-column>
      <el-table-column
              label="Whether to lock this account"
               width="200">
        <template slot-scope="scope">
        <el-switch
                v-model="scope.row.accountNonLocked"
                @change="changeLockedStatus(scope.row)"
                active-text="Unlock"
                inactive-text="Lock"
                active-color="#13ce66"
                inactive-color="#ff4949">
        </el-switch>
        </template>
      </el-table-column>
      <el-table-column label="Operation">
        <template slot-scope="scope">
          <el-button
                  size="mini"
                  @click="handleEdit(scope.$index, scope.row)">Edit</el-button>
          <el-button
                  size="mini"
                  type="danger"
                  @click="handleDelete(scope.row)">Delete</el-button>
        </template>
      </el-table-column>
    </el-table>
    <div style="display: flex;justify-content: space-between;margin-top: 10px">
      <el-button @click="handleMultiDelete" :disabled="multipleSelection.length==0?true:false" type="danger" icon="el-icon-delete">Batch Delete</el-button>
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
</div>
</template>

<script>
  export default {
    name: "UserInfo",
    data(){
      return{
        stateValue:-1,
        stateOptions:[
          {
            label:'Select',
            value:-1
          },
          {
            label:'UnLock',
            value:0
          },
          {
            label:'Locked',
            value:1
          }
        ],
       userData:[],
       total:0,
        page:1,
        size:10,
        nameKeyword:'',
        multipleSelection: [],
        loading:false,
      }
    },
    mounted(){
      this.initUserData();
    },
    methods:{

      initUserData(){
        this.loading=true;
        let url="/user/?page="+this.page+"&size="+this.size;
        if (this.nameKeyword!=''){
          url+="&keyword="+this.nameKeyword;
        }
        if (this.stateValue!=-1){
          url+="&isLocked="+this.stateValue;
        }
        this.getRequest(url).then(resp=>{
          if (resp){
            this.userData=resp.data;
            this.total=resp.total;
            setTimeout(()=>{
              this.loading=false;
            },1000)
          }
        })
      },
 
      handleSizeChange(val){
        this.size=val;
        this.initUserData();
      },

      handleCurrentChange(val){
        this.page=val;
        this.initUserData();
      },

      refreshTable(){
        this.nameKeyword='';
        this.stateValue=-1;
        this.initUserData();
      },

      changeLockedStatus(data){
        this.putRequest("/user/?id="+data.id+"&isLocked="+!data.accountNonLocked).then(resp=>{
          if(resp){
            this.initUserData();
          }
        })
  },

  handleDelete(data){
         this.$confirm('This operation will permanently delete the user ['+data.nickname+'], do you want to continue?', 'Prompt', {
           confirmButtonText: 'OK',
           cancelButtonText: 'Cancel',
           type: 'warning'
         }).then(() => {
           this.deleteRequest("/user/"+data.id).then(resp=>{
             if (resp){
   
               this.initUserData();
             }
           })
         }).catch(() => {
           this.$message({
             type: 'info',
             message: 'Deletion canceled'
           });
         });
       },

      handleSelectionChange(val){
        this.multipleSelection=val;
      },

      handleMultiDelete(){
         this.$confirm('This operation will permanently delete ['+this.multipleSelection.length+'] users, do you want to continue?', 'Prompt', {
           confirmButtonText: 'OK',
           cancelButtonText: 'Cancel',
           type: 'warning'
         }).then(() => {
           let url="/user/?";
           this.multipleSelection.forEach(item=>{
             url+="ids="+item.id+"&";
           })
           this.deleteRequest(url).then(resp=>{
             if (resp){
               this.initUserData();
             }
           })
         }).catch(() => {
           this.$message({
             type: 'info',
             message: 'Deletion canceled'
           });
         });

       }
    }
  }
</script>

<style scoped>

</style>
