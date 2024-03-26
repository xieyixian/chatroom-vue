<template>
  <div>
    <div>
      User Account Status:
      <el-select v-model="stateValue" placeholder="Please select">
        <el-option
                v-for="item in stateOptions"
                :key="item.value"
                :label="item.label"
                :value="item.value">
        </el-option>
      </el-select>
      <el-input placeholder="Search by user nickname"
                prefix-icon="el-icon-search"
                style="width: 200px;margin: 0 10px"
                 v-model="nameKeyword"></el-input>
      <el-button @click="initUserData" icon="el-icon-search" type="primary">Search</el-button>
      <el-button @click="refreshTable" icon="el-icon-refresh" type="primary">Refresh Table</el-button>
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
                label="ID"
                width="80">
        </el-table-column>
        <el-table-column
                prop="username"
                label="Username"
                width="180">
        </el-table-column>
        <el-table-column
                prop="nickname"
                label="Nickname"
                width="180">
        </el-table-column>
        <el-table-column
                label="User Avatar"
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
                label="Account Lock Status"
                 width="200">
          <template slot-scope="scope">
          <el-switch
                  v-model="scope.row.accountNonLocked"
                  @change="changeLockedStatus(scope.row)"
                  active-text="Unlocked"
                  inactive-text="Locked"
                  active-color="#13ce66"
                  inactive-color="#ff4949">
          </el-switch>
          </template>
        </el-table-column>
        <el-table-column label="Actions">
          <template slot-scope="scope">
            <!-- <el-button
                    size="mini"
                    @click="handleEdit(scope.$index, scope.row)">Edit</el-button> -->
            <el-button
                    size="mini"
                    type="danger"
                    @click="handleDelete(scope.row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="display: flex;justify-content: space-between;margin-top: 10px">
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
  </div>
  </template>
  
  <script>
    export default {
      name: "UserInfo",
      data(){
        return{
          stateValue:-1, // Default user state
          stateOptions:[
            {
              label:'Please select',
              value:-1
            },
            {
              label:'Unlocked',
              value:0
            },
            {
              label:'Locked',
              value:1
            }
          ],
         userData:[],
         total:0, // Total number of table data
          page:1, // Current page number
          size:10, // Number of records per page
          nameKeyword:'', // Search keyword
          multipleSelection: [], // Array of selected items
          loading:false, // Loading animation
        }
      },
      mounted(){
        this.initUserData();
      },
      methods:{
        // Fetch table data
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
      // Callback function for changing the current page size
      handleSizeChange(val){
        this.size=val;
        this.initUserData();
      },
      // Callback function for changing the current page number
      handleCurrentChange(val){
        this.page=val;
        this.initUserData();
      },
      // Refresh the table
      refreshTable(){
        this.nameKeyword='';
        this.stateValue=-1;
        this.initUserData();
      },
      // Change the lock status of a user account
      changeLockedStatus(data){
        this.putRequest("/user/?id="+data.id+"&isLocked="+!data.accountNonLocked).then(resp=>{
          if(resp){
            this.initUserData();
          }
        })
      },
      // Delete a single user
      handleDelete(data){
        this.$confirm('This action will permanently delete the user ['+data.nickname+'], do you want to proceed?', 'Warning', {
          confirmButtonText: 'OK',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.deleteRequest("/user/"+data.id).then(resp=>{
            if (resp){
              // Refresh the table
              this.initUserData();
            }
          })
        }).catch(() => {
          this.$message({
            type: 'info',
            message: 'Deletion cancelled'
          });
        });
      },
      // Triggered when the selection changes
      handleSelectionChange(val){
        this.multipleSelection=val;
      },
      // Handle bulk deletion of users
      handleMultiDelete(){
        this.$confirm('This action will permanently delete ['+this.multipleSelection.length+'] users, do you want to proceed?', 'Warning', {
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
            message: 'Deletion cancelled'
          });
        });

      }
    }
  }
</script>

<style scoped>

</style>

  