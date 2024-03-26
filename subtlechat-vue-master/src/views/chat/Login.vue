<template>
  <el-container>
    <el-header >
      <p class="text">
      </p>
      <el-button @click="gotoAdminLogin" icon="el-icon-d-arrow-right" style="float: right;border: none" >Admin Login</el-button>
    </el-header>
    <el-main>
      <div class="loginContainer">
        <el-form ref="loginForm" :rules="rules" :model="loginForm" label-width="80px">
          <h3 class="loginTitle">Team7 Chat Room</h3>
          <el-form-item label="username:" prop="username">
            <el-input type="text" v-model="loginForm.username" auto-complete="off" placeholder="please enter username"></el-input>
          </el-form-item>
          <el-form-item label="password:" prop="password">
            <el-input type="password"  v-model="loginForm.password" auto-complete="off" placeholder="please enter password"></el-input>
          </el-form-item>
          <el-form-item label="verifycode:" prop="code">
            <el-input type="text" @keydown.enter.native="submitLogin" v-model="loginForm.code" auto-complete="off" placeholder="please enter verify code" style="width:150px;"></el-input>
            <img :src="verifyCode" title="Click to switch verification code" @click="changeverifyCode" />
          </el-form-item>
          <el-checkbox v-model="checked" class="loginRemember"></el-checkbox><span> Remeber Me</span>
          <div>
            <el-button @click="showRegistryDialog" style="width:45% ;margin-right: 15px">Register</el-button>
            <el-button type="primary" style="width:45% ;" @click="submitLogin"  v-loading.fullscreen.lock="fullscreenLoading">Login</el-button>
          </div>
        </el-form>
      </div>
    </el-main>
    <el-dialog title="register" :before-close="closeRegisterDialog" :visible.sync="registerDialogVisible" width="30%">
      <el-form :model="registerForm" status-icon :rules="registerRules" ref="registerForm" >
        <el-form-item label="nickname：" :label-width="formLabelWidth" prop="nickname">
          <el-input v-model=" registerForm.nickname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="username：" :label-width="formLabelWidth" prop="username">
            <el-input v-model="registerForm.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="password：" :label-width="formLabelWidth" prop="password">
          <el-input type="password" v-model="registerForm.password" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="password-check：" :label-width="formLabelWidth" prop="checkPass">
          <el-input type="password" v-model="registerForm.checkPass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="avtur：" :label-width="formLabelWidth">
          <el-upload
                  action="/file"
                  ref="upload"
                  list-type="picture-card"
                  :class="{disabled:uploadDisabled}"
                  :before-upload="beforeAvatarUpload"
                  :file-list="fileList"
                  :on-progress="onProgress"
                  :on-success="imgSuccess"
                  :on-error="imgError"
                  :on-remove="imgRemove"
                  >
            <i  class="el-icon-plus"></i>
            <div slot="tip" class="el-upload__tip">Only images no larger than 4MB can be uploaded (default avatar can be used!)</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="email:" prop="email">
          <el-input type="text"  v-model="registerForm.email" auto-complete="off" placeholder="please enter email" style="width: 120px;margin-right: 10px" ></el-input>
          <el-button @click="getMailVerifyCode" :disabled="getCodeEnable"  size="mini">{{getCodeBtnText}}</el-button>
        </el-form-item>

        <el-form-item label="verifycode:" prop="mailCode">
          <el-input type="text"  v-model="registerForm.mailCode" auto-complete="off" placeholder="please enter the verifycode" style="width: 120px;margin-right: 10px" ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRegisterForm('registerForm') " style="width: 100%">register</el-button>
      </div>
    </el-dialog>
  </el-container>


</template>

<script>
import JSEncrypt from 'jsencrypt';
import axios from 'axios';
import { MessageBox } from 'element-ui';
const vm = this;
  export default {
    name: "Login",
    data(){
      var validateNickname = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('Please enter a nickname'));
        }

          this.getRequest("user/checkNickname?nickname="+value).then(resp=>{
            if (resp!=0){
              callback(new Error("This nickname has been registered"))
            } else {
              callback();
            }
          })
      };
      var validateUsername = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('please enter user name'));
        }

        this.getRequest("/user/checkUsername?username="+value).then(resp=>{
            if (resp!=0){
              callback(new Error('this username has been registered'));
            }
            else {
              callback();
            }
          })

      };
      var validatePass = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('Please enter password'));
        } else {
          if (this.registerForm.checkPass !== '') {
            this.$refs.registerForm.validateField('checkPass');
          }
          callback();
        }
      };
      var validatePass2 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('Please enter password again'));
        } else if (value !== this.registerForm.password) {
          callback(new Error('The password entered twice is inconsistent!'));
        } else {
          callback();
        }
      };
      return{
        loginForm:{
           username:'',
           password:'',
           code:'',
           type:'login',
        },
        verifyCode:'/verifyCode',
        checked:true,
        rules: {
          username:[{required:true,message:'please enter user name',trigger:'blur'}],
          password:[{required:true,message: 'Please enter password',trigger:'blur'}],
          code:[{required:true,message: 'please enter verification code',trigger:'blur'}],
          email:[{required:true,message: 'please input your email',trigger:'blur'}],
          mailCode:[{required:true,message: 'please enter verification code',trigger:'blur'}]
        },
        fullscreenLoading:false,

        registerDialogVisible:false,
        getCodeBtnText:'get mail code',
        getCodeEnable:false,
        formLabelWidth: '120px',
        registerForm:{
          nickname:'',
          username:'',
          password:'',
          checkPass:'',
          email:'',
          mailCode:'',
          userProfile:'default_head.jpg',
        },
        registerRules: {
          nickname: [
            { validator: validateNickname, trigger: 'blur' }
          ],
          username: [
            { validator: validateUsername, trigger: 'blur' }
          ],
          password: [
            { validator: validatePass, trigger: 'blur' }
          ],
          checkPass: [
            { validator: validatePass2, trigger: 'blur' }
          ],
        },
        uploadDisabled:false,

        fileList:[],
      };
    },
    created() {


      axios.get('/getPublicKey')
          .then(response => {
            const responseData = response;
            console.log("Receive public key: " + responseData)

            sessionStorage.setItem('publicKey', JSON.stringify(responseData));
          })
    },
    methods:{

        encryptData() {
          const encryptor = new JSEncrypt();
          let storedKey = sessionStorage.getItem('publicKey');
          storedKey = storedKey.substring(1, storedKey.length - 1);
          encryptor.setPublicKey(storedKey);

          sessionStorage.setItem("encryptedUsername",this.loginForm.username);

          this.loginForm.username = encryptor.encrypt(this.loginForm.username);
          this.loginForm.password = encryptor.encrypt(this.loginForm.password);
          console.log("Data is Encrypted, the data is: " + this.loginForm.username+" and "+ this.loginForm.password);
        },


        encryptData_register() {
          const encryptor = new JSEncrypt();

          let storedKey = sessionStorage.getItem('publicKey');
          storedKey = storedKey.substring(1, storedKey.length - 1);;
          encryptor.setPublicKey(storedKey);

          this.registerForm.username = encryptor.encrypt(this.registerForm.username);
          this.registerForm.password = encryptor.encrypt(this.registerForm.password);

        },
        decryptData(obj) {

          const decryptor = new JSEncrypt();

          let storedKey = sessionStorage.getItem('publicKey');

          storedKey = storedKey.substring(1, storedKey.length - 1);
          console.log(storedKey);

          decryptor.setPublicKey(storedKey);

          let decryptedObj = { ...obj }; // Create a copy of the original object

          if (decryptedObj.username) {
            decryptedObj.username = decryptor.decrypt(decryptedObj.username);
          }
          if (decryptedObj.email) {
            decryptedObj.email = decryptor.decrypt(decryptedObj.email);
          }

          return decryptedObj;
        },


        async checkIPAddress() {
  try {
    const response = await fetch('http://localhost:5000/ipcheck', {
      method: 'POST',
      headers: {
        'Content-Type': 'application/json'
      },
      body: JSON.stringify({ })
    });
    
    const data = await response.json();

    return data.prediction;
  } catch (error) {
    console.error('IP check request failed:', error);
    throw new Error('IP check request failed');
  }
},

async submitLogin() {

  const ipcheck = await this.checkIPAddress();

  if (ipcheck !== 2) {
    let message;

    if (ipcheck === 0) {
      message = 'Your IP check passed, do you want to continue logging in??';
    } else {
      message = 'Your IP may be a threat. Do you want to continue logging in?';
    }
     console.log('message:', message);
    try {
      await this.$confirm(message, 'confirm', {
        confirmButtonText: 'confirm',
        cancelButtonText: 'Cancel',
        type: 'warning'
      });


      this.$refs.loginForm.validate(async (valid) => {
        if (valid) {
          this.fullscreenLoading = true;
          this.encryptData();

          try {
            const resp = await this.postKeyValueRequest('/doLogin', this.loginForm);
            setTimeout(() => {
              this.fullscreenLoading = false;
            }, 1000);

            if (resp) {

              this.$store.state.currentUser = resp.obj.user;
              sessionStorage.setItem("encryptedPassword", this.loginForm.password);
              sessionStorage.setItem("code", this.loginForm.code);
              this.$router.replace("/mailLogin");
            } else {

              this.loginForm.username = '';
              this.loginForm.password = '';
              this.changeverifyCode();
            }
          } catch (error) {
            this.fullscreenLoading = false;
            console.error('Login request failed:', error);
          }
        } else {
          this.$message.error("Username, password and verification code cannot be empty!");
        }
      });
    } catch (error) {

      console.log('User canceled the operation');
    }
  } else {

    console.log("Blacklist cannot log in to the system:", ipcheck);
  }
},

      changeverifyCode(){
        this.verifyCode="/verifyCode?time="+new Date();
      },
      gotoAdminLogin(){
        this.$router.replace("/adminlogin");
      },
      showRegistryDialog(){
        this.registerDialogVisible=true;
      },

      beforeAvatarUpload(file) {
        let isLt4M = file.size / 1024 / 1024 < 4;

        if (!isLt4M) {
          this.$message.error('The size of the uploaded avatar image cannot exceed 4MB!');
        }
        return isLt4M;
      },

      onProgress(event, file, fileList){
        this.uploadDisabled = true;
      },

      imgSuccess(response, file, fileList) {
        this.uploadDisabled = true;
        this.registerForm.userProfile=response;
        console.log("The image url is:"+this.registerForm.userProfile);
      },
  
      imgError(err, file, fileList){
        this.$message.error("upload failed");
        this.uploadDisabled = false;
      },

      imgRemove(file,fileList){
        this.uploadDisabled = false;
      },
      closeRegisterDialog(done){
        this.registerForm={
          nickname:'',
          username:'',
          password:'',
          checkPass:'',
          userProfile:'',
        };
        //this.$refs.upload.clearFiles();
        done();
      },

      submitRegisterForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.postRequest("/user/register",this.registerForm).then(resp=>{
              if (resp){
                this.registerDialogVisible=false;
                location.reload();
              }else{
                location.reload();
              }
            })
          } else {
            this.$message.error("Please fill in the information correctly!");
            console.log('error submit!!');
            return false;
          }
        });
      },

      async getMailVerifyCode() {
        const email = this.registerForm.email;

        try {
          const response = await axios.post('/user/mailVerifyCode', { email: email });

          if (response.data) {
            this.getCodeEnable = true;

            let i = 30;
            let id = setInterval(() => {
              this.getCodeBtnText = i-- + "s Cannot send within ";
            }, 1000);
            setTimeout(() => {
              clearInterval(id);
              this.getCodeEnable = false;
              this.getCodeBtnText = "Get email verification code";
            }, 30000);
          }
        } catch (error) {
          console.error('Error while getting mail verification code:', error);

        }

      },

      async getMailVerifyCodeForlogin() {
        const username = this.loginForm.username;

        try {
          const response = await axios.post('/user/loginMailVerifyCode', { username: username });

          if (response.data) {
            this.getCodeEnable = true;

            let i = 30;
            let id = setInterval(() => {
              this.getCodeBtnText = i-- + "s Cannot send within";
            }, 1000);
            setTimeout(() => {
              clearInterval(id);
              this.getCodeEnable = false;
              this.getCodeBtnText = "Get email verification code";
            }, 30000);
          }
        } catch (error) {
          console.error('Error while getting mail verification code:', error);

        }
      }

    }
  }
</script>


<style>
  .disabled .el-upload--picture-card{
    display: none;
  }
  .loginContainer{
    width: 350px;
    margin: 100px auto;
    border-radius:15px ;
    border: 1px solid #eaeaea;

    box-shadow: 10px 10px 35px #cac6c6;
    background: #fff;

    background-clip: padding-box;
    padding: 25px 35px 25px 35px;
  }
  .loginTitle{
    margin: 10px auto 30px auto;
    text-align: center;
    color:#505458;
  }
  .loginRemember{
    margin: 5px auto 35px 80px;
  }
  /*.el-form-item__content{*/
  /* display: flex;*/
  /*  align-items: center*/
  /*}*/
</style>