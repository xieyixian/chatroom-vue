<template>
  <el-container>
    <el-header class="homeHeader">
      <div class="title">management terminal</div>
      <div>
        <el-dropdown class="choices" @command="commandHandler">
        <span class="el-dropdown-link">
          {{user.name}}<i><img :src="user.userProfile"></i>
        </span>
          <el-dropdown-menu slot="dropdown">
            <el-dropdown-item command="userInfo">Personal Center</el-dropdown-item>
            <el-dropdown-item command="setting">Setting</el-dropdown-item>
            <el-dropdown-item command="logout" divided>Logout</el-dropdown-item>
          </el-dropdown-menu>
        </el-dropdown>
      </div>

    </el-header>
    <el-container>
      <el-aside width="200px">
        <el-menu router unique-opened>
          <el-submenu :index="index+''" v-for="(item,index) in routes" v-if="item.hidden!=true" :key="index">
            <template slot="title">
              <i style="color: #2F86D2;margin-right: 8px" :class="item.iconCls"></i>
              <span>{{item.name}}</span>
            </template>
              <el-menu-item :index="child.path" v-for="(child,indexj) in item.children" :key="indexj">{{child.name}}</el-menu-item>
          </el-submenu>
        </el-menu>
      </el-aside>
      <el-main>
        <el-breadcrumb separator-class="el-icon-arrow-right" v-if="this.$router.currentRoute.path!='/home'">
          <el-breadcrumb-item :to="{ path: '/home' }">Home</el-breadcrumb-item>
          <el-breadcrumb-item>{{this.$router.currentRoute.name}}</el-breadcrumb-item>
        </el-breadcrumb>
        <div class="homeWelcome" v-if="this.$router.currentRoute.path=='/home'">
          Welcome
        </div>
        <router-view class="homeRouterView"/>
      </el-main>
    </el-container>
  </el-container>
</template>

<script>
  export default {
    name: "Home",
    data(){
      return{
        user:JSON.parse(window.sessionStorage.getItem("admin"))
      }
    },
    computed:{
      routes(){
       return this.$router.options.routes;
      }
    },
    methods:{
      test(){
        this.$router.push("/userinfo")
      },
      commandHandler(cmd){

        if (cmd=='logout'){
          this.$confirm('This operation will log you out, do you want to continue?', 'Prompt', {
             confirmButtonText: 'OK',
             cancelButtonText: 'Cancel',
             type: 'warning'
           }).then(() => {
             this.getRequest("/admin/logout");
             sessionStorage.removeItem("admin");
             this.$router.replace('/adminlogin');
          }).catch(() => {
            this.$message({
              type: 'info',
              message: 'Cancel'
            });
          });
        }else if (cmd=='userInfo'){
          //this.$router.push('/hrinfo');
        }
      }
    }
  }
</script>

<style>
  .homeHeader{
    background-color: #409eff;
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding:0px;
    box-sizing:border-box;
  }
  .homeHeader .title{
    font-size: 30px;
    font-family: 华文行楷;
    color:#ffffff;
  }
  .homeHeader .choices{
    cursor: pointer;
  }
  .choices img{
    width: 48px;
    height: 48px;
    border-radius: 24px;
    margin-left: 10px;
  }
  .el-dropdown-link{
    display: flex;
    align-items: center;
  }
  .homeWelcome{
    text-align: center;
    font-size: 30px;
    font-family: 华文行楷;
    color: #409eff;
  }
  .homeRouterView{
    margin-top: 15px;
  }
</style>
