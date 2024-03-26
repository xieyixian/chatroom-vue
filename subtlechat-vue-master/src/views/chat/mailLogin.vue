<template>
    <el-container>
      <el-main>
        <div class="loginContainer">
          <el-form ref="loginForm" :rules="rules" :model="loginForm" label-width="80px">
            <h3 class="loginTitle">Team7 Chat Room</h3>

            <el-form-item label="username:" prop="username">
              <el-input type="text" v-model="loginForm.username" auto-complete="off" placeholder="please enter the username"></el-input>
            </el-form-item>
            
            <el-form-item label="verify code:" prop="mailCode">
               <el-input type="text"  v-model="loginForm.mailCode" auto-complete="off" placeholder="please enter the Verify Code" style="width: 120px;margin-right: 10px" ></el-input>   
               <el-button @click="getMailVerifyCodeForlogin" :disabled="getCodeEnable"  size="mini">{{getCodeBtnText}}</el-button>
            </el-form-item>
          </el-form>

        <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitLogin('loginForm') " style="width: 100%">Login</el-button>
        </div>
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
             mailCode:'',
             code:'',
             type:'',
          },
          checked:true,
          rules: {
            username:[{required:true,message:'please enter user name',trigger:'blur'}],
            mailCode:[{required:true,message: 'please enter verification code',trigger:'blur'}]
          },
          fullscreenLoading:false,
          getCodeBtnText:'get email code',
          getCodeEnable:false,
          formLabelWidth: '120px',

          uploadDisabled:false,
        };
      },


      created() {
  
        axios.get('/getPublicKey')
        .then(response => {
          const responseData = response;
          console.log("Receive public key: " + responseData)
          sessionStorage.setItem('publicKey', JSON.stringify(responseData));
          this.loginForm.username=sessionStorage.getItem("encryptedUsername")
 

        })
      },
      methods:{
  
        encryptData() {
      const encryptor = new JSEncrypt();
      let storedKey = sessionStorage.getItem('publicKey');
      storedKey = storedKey.substring(1, storedKey.length - 1);;
      encryptor.setPublicKey(storedKey);
      this.loginForm.username = encryptor.encrypt(this.loginForm.username);
      console.log("Data is Encrypted, the data is: " + this.loginForm.username);
      },
      decryptData(obj) {
      const decryptor = new JSEncrypt();
      let storedKey = sessionStorage.getItem('publicKey');
      storedKey = storedKey.substring(1, storedKey.length - 1);
      console.log(storedKey);
      decryptor.setPublicKey(storedKey);

      let decryptedObj = { ...obj }; // Create a copy of the original object
      // Decrypt username and email fields if they exist
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
              this.loginForm.password=sessionStorage.getItem("encryptedPassword")
              this.loginForm.code=sessionStorage.getItem("code")
              this.loginForm.type='mailVerify'
              this.encryptData();
              console.log("Data is Encrypted, the username is: " + this.loginForm.username);
              console.log("Data is Encrypted, the password is: " + this.loginForm.password);
              this.postKeyValueRequest('/doLogin',this.loginForm).then(resp=>{
                setTimeout(()=>{
                  this.fullscreenLoading=false;
                },1000);
                if (resp){
                 // console.log("1111111111enter success",resp);
                  resp.obj=this.decryptData(resp.obj);
                  console.log("enter success");
                  this.$store.state.currentUser=resp.obj.user;
                  window.sessionStorage.setItem("user",JSON.stringify(resp.obj.user));
                  let path=this.$route.query.redirect;
                  this.$router.replace((path=='/mailLogin'||path==undefined)?"/chatroom":path);
                }else {
                  this.loginForm.username='';
                  this.loginForm.password='';
                }
              })
            } else {
              this.$message.error("Username and verification code cannot be empty!");
              return false;
            }
          });
        },

   
        beforeAvatarUpload(file) {
          let isLt4M = file.size / 1024 / 1024 < 4;
  
          if (!isLt4M) {
            this.$message.error('Username and verification code cannot be empty! 4MB!');
          }
          return isLt4M;
        },

        async getMailVerifyCodeForlogin() {
          const username = this.loginForm.username;
          try {
            const response = await axios.post('/user/loginMailVerifyCode', { username: username });
            
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
  