<template>
  <el-container>
    <el-header>
      <el-button @click="gotoClientLogin" icon="el-icon-d-arrow-left" style="border: none" >user login</el-button>
    </el-header>
    <el-main>
      <div class="loginContainer">
        <el-form ref="loginForm" :rules="rules" :model="loginForm" label-width="80px">
          <h3 class="loginTitle">Admin Login</h3>
          <el-form-item label="username:" prop="username">
            <el-input type="text" v-model="loginForm.username" auto-complete="off" placeholder="please enter username"></el-input>
          </el-form-item>
          <el-form-item label="password:" prop="password">
            <el-input type="password"  v-model="loginForm.password" auto-complete="off" placeholder="please enter password"></el-input>
          </el-form-item>
          <el-form-item label="verify_code:" prop="mailCode">
             <el-input type="text"  v-model="loginForm.mailCode" auto-complete="off" placeholder="please enter verify code" style="width: 120px;margin-right: 10px" ></el-input>

             <el-button @click="getMailVerifyCode" :disabled="getCodeEnable"  size="mini">{{getCodeBtnText}}</el-button>
          </el-form-item>
          <el-checkbox v-model="checked" class="loginRemember"></el-checkbox><span> Remeber me</span>
          <el-button type="primary" style="width:100% ;" @click="submitLogin"  v-loading.fullscreen.lock="fullscreenLoading">Login</el-button>
        </el-form>
      </div>
    </el-main>
  </el-container>
</template>

<script>
import JSEncrypt from 'jsencrypt';
import axios from 'axios';
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
          username:[{required:true,message:'please enter user name',trigger:'blur'}],
          password:[{required:true,message: 'Please enter password',trigger:'blur'}],
          mailCode:[{required:false,message: 'please enter verification code',trigger:'blur'}]
        },
        fullscreenLoading:false,
        getCodeEnable:false,
        getCodeBtnText:'get email code',
      }
    },
    created() {

      axios.get('/getPublicKey')
          .then(response => {
            const responseData = response;
            //console.log(responseData)
            sessionStorage.setItem('publicKey', JSON.stringify(responseData));
          })
    },
    methods:{
      encryptData() {
        const encryptor = new JSEncrypt();
        let storedKey = sessionStorage.getItem('publicKey');
        storedKey = storedKey.substring(1, storedKey.length - 1);
        console.log(storedKey);
        encryptor.setPublicKey(storedKey);

        this.loginForm.username = encryptor.encrypt(this.loginForm.username);
        this.loginForm.password = encryptor.encrypt(this.loginForm.password);
      },
      decryptData(obj) {
        const decryptor = new JSEncrypt();
        let storedKey = sessionStorage.getItem('publicKey');
        storedKey = storedKey.substring(1, storedKey.length - 1);
        console.log(storedKey);

        decryptor.setPublicKey(storedKey);

        let decryptedObj = { ...obj };

        if (decryptedObj.username) {
          decryptedObj.username = decryptor.decrypt(decryptedObj.username);
        }
        if (decryptedObj.email) {
          decryptedObj.email = decryptor.decrypt(decryptedObj.email);
        }
        return decryptedObj;
      },

      submitLogin(){
        this.$refs.loginForm.validate((valid) => {
          if (valid) {
            this.fullscreenLoading=true;
            this.encryptData();
            this.postKeyValueRequest('/admin/doLogin',this.loginForm).then(resp=>{
              setTimeout(()=>{
                this.fullscreenLoading=false;
              },1000);
              if (resp){

                resp.obj=this.decryptData(resp.obj);
                 window.sessionStorage.setItem("admin",JSON.stringify(resp.obj));
                 this.$router.replace("/home");
              }
              else{
                this.loginForm.username='';
                this.loginForm.password='';
                this.loginForm.mailCode='';
              }
            })
          } else {
            this.$message.error("Username, password and verification code cannot be empty!");
            return false;
          }
        });
      },
      gotoClientLogin(){
        this.$router.replace("/");
      },

      getMailVerifyCode(){
        const adminUsername = this.loginForm.username;
        window.sessionStorage.setItem('adminUsername', adminUsername);

        this.postRequest("/admin/mailVerifyCode", {username: adminUsername}).then(resp=>{
          if (resp){
            this.getCodeEnable=true;

              let i=30;
            let id=setInterval(()=>{
              this.getCodeBtnText=i--+"s Cannot send within";
            },1000);
            setTimeout(()=>{
              clearInterval(id);
              this.getCodeEnable=false;
              this.getCodeBtnText="Get email verification code";
            },30000)
          }
        })
      }
    }
  }
</script>

<style>

</style>
