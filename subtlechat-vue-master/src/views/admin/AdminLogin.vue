<template>
  <el-container>
    <el-header>
      <el-button @click="gotoClientLogin" icon="el-icon-d-arrow-left" style="border: none" >Client Login</el-button>
    </el-header>
    <el-main>
      <div class="loginContainer">
        <el-form ref="loginForm" :rules="rules" :model="loginForm" label-width="80px">
          <h3 class="loginTitle">Admin Login</h3>
          <el-form-item label="Username:" prop="username">
            <el-input type="text" v-model="loginForm.username" auto-complete="off" placeholder="Please enter username"></el-input>
          </el-form-item>
          <el-form-item label="Password:" prop="password">
            <el-input type="password" v-model="loginForm.password" auto-complete="off" placeholder="Please enter password"></el-input>
          </el-form-item>
          <el-form-item label="Verification Code:" prop="mailCode">
            <el-input type="text" v-model="loginForm.mailCode" auto-complete="off" placeholder="No verification code needed" style="width: 120px;margin-right: 10px" ></el-input>
            <el-button @click="getMailVerifyCode" :disabled="getCodeEnable" size="mini">{{getCodeBtnText}}</el-button>
          </el-form-item>
          <el-checkbox v-model="checked" class="loginRemember"></el-checkbox><span> Remember me for a week</span>
          <el-button type="primary" style="width:100% ;" @click="submitLogin" v-loading.fullscreen.lock="fullscreenLoading">Log In</el-button>
        </el-form>
      </div>
    </el-main>
  </el-container>
</template>

<script>

  export default {
    name: "Login",
    data(){
      return{
        loginForm:{
           username:'',
           password:'',
           mailCode:''
        },
        checked:true,
        rules: {
          username:[{required:true,message:'Please enter your username',trigger:'blur'}],
          password:[{required:true,message: 'Please enter password',trigger:'blur'}],
          //开发环境 mailCode:[{required:true,message: '请输入验证码',trigger:'blur'}]
          mailCode:[{required:false,message: 'Please enter the verification code',trigger:'blur'}]
        },
        fullscreenLoading:false,
        getCodeEnable:false,
        getCodeBtnText:'Get the email verification code',
      }
    },
    methods:{
      submitLogin(){
        this.$refs.loginForm.validate((valid) => {
          if (valid) {
            this.fullscreenLoading=true;
            this.postKeyValueRequest('/admin/doLogin',this.loginForm).then(resp=>{
              setTimeout(()=>{
                this.fullscreenLoading=false;
              },1000);
              if (resp){
                //alert("登录成功！");
                 //保存登录用户到session中
                 window.sessionStorage.setItem("admin",JSON.stringify(resp.obj));
                 this.$router.replace("/home");
              }
            })
          } else {
            this.$message.error("The username, password, and verification code cannot be empty！");
            return false;
          }
        });
      },
      gotoClientLogin(){
        this.$router.replace("/");
      },
      //获取邮箱验证码
      getMailVerifyCode(){
        this.getRequest("/admin/mailVerifyCode").then(resp=>{
          if (resp){
            this.getCodeEnable=true;
            //30s内不得再次发送
              let i=30;
            let id=setInterval(()=>{
              this.getCodeBtnText=i--+"s Internal cannot send";
            },1000);
            setTimeout(()=>{
              clearInterval(id);
              this.getCodeEnable=false;
              this.getCodeBtnText="Get the email verification code";
            },30000)
          }
        })
      }
    }
  }
</script>

<style>

</style>
