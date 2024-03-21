<template>
    <el-container>
      <el-main>
        <div class="loginContainer">
          <el-form ref="loginForm" :rules="rules" :model="loginForm" label-width="80px">
            <h3 class="loginTitle">微言SubtleChat~</h3>

            <el-form-item label="用户名:" prop="username">
              <el-input type="text" v-model="loginForm.username" auto-complete="off" placeholder="请输入用户名"></el-input>
            </el-form-item>
            
            <el-form-item label="验证码:" prop="mailCode">
               <el-input type="text"  v-model="loginForm.mailCode" auto-complete="off" placeholder="请输入验证码" style="width: 120px;margin-right: 10px" ></el-input>   
               <el-button @click="getMailVerifyCodeForlogin" :disabled="getCodeEnable"  size="mini">{{getCodeBtnText}}</el-button>
            </el-form-item>
          </el-form>

        <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitLogin('loginForm') " style="width: 100%">登录</el-button>
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
            username:[{required:true,message:'请输入用户名',trigger:'blur'}],
            mailCode:[{required:true,message: '请输入验证码',trigger:'blur'}]
          },
          fullscreenLoading:false,
          getCodeBtnText:'获取邮箱验证码',
          getCodeEnable:false,
          formLabelWidth: '120px',

          uploadDisabled:false,
        };
      },


      created() {
        // 假设 this.item 是需要存储的数据
  
        axios.get('/getPublicKey')
        .then(response => {
          const responseData = response;
          console.log("Receive public key: " + responseData)
          // 将数据存储到 session 中
          sessionStorage.setItem('publicKey', JSON.stringify(responseData));
          this.loginForm.username=sessionStorage.getItem("encryptedUsername")
 

        })
      },
      methods:{
  
        encryptData() {
      // 创建JSEncrypt实例
      const encryptor = new JSEncrypt();

      // 从 session 中获取数据
      let storedKey = sessionStorage.getItem('publicKey');

      storedKey = storedKey.substring(1, storedKey.length - 1);;
      // 设置公钥
      encryptor.setPublicKey(storedKey);


      // 加密数据
      this.loginForm.username = encryptor.encrypt(this.loginForm.username);

      console.log("Data is Encrypted, the data is: " + this.loginForm.username);
      },
      decryptData(obj) {
      // 创建JSEncrypt实例
      const decryptor = new JSEncrypt();

      // 从 session 中获取公钥
      let storedKey = sessionStorage.getItem('publicKey');

      storedKey = storedKey.substring(1, storedKey.length - 1);
      console.log(storedKey);
      
      // 设置公钥
      decryptor.setPublicKey(storedKey);

      // 解密数据
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

                  resp.obj=this.decryptData(resp.obj);
                  console.log("enter success");
                  //保存当前用户到vuex
                  this.$store.state.currentUser=resp.obj;
                  //保存登录用户到sessionStorage中
                  window.sessionStorage.setItem("user",JSON.stringify(resp.obj));
                  let path=this.$route.query.redirect;
                  this.$router.replace((path=='/mailLogin'||path==undefined)?"/chatroom":path);
                }else {
                  this.loginForm.username='';
                  this.loginForm.password='';
                }
              })
            } else {
              this.$message.error("用户名和验证码都不能为空！");
              return false;
            }
          });
        },

        /**
         *   图片上传的方法
         */
        //上传前
        beforeAvatarUpload(file) {
          let isLt4M = file.size / 1024 / 1024 < 4;
  
          if (!isLt4M) {
            this.$message.error('上传头像图片大小不能超过 4MB!');
          }
          return isLt4M;
        },

        // 登录获取邮箱验证码
        async getMailVerifyCodeForlogin() {
          const username = this.loginForm.username;
          // 发送POST请求
          try {
            const response = await axios.post('/user/loginMailVerifyCode', { username: username });
            
            if (response.data) {
              this.getCodeEnable = true;
              // 30s内不得再次发送
              let i = 30;
              let id = setInterval(() => {
                this.getCodeBtnText = i-- + "s内不能发送";
              }, 1000);
              setTimeout(() => {
                clearInterval(id);
                this.getCodeEnable = false;
                this.getCodeBtnText = "获取邮箱验证码";
              }, 30000);
            }
          } catch (error) {
            console.error('Error while getting mail verification code:', error);
            // 处理错误情况
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
      /*添加阴影 h-shadow(水平阴影位置)，v-shadow(垂直阴影位置)，blur(阴影大小)，color(颜色)*/
      box-shadow: 10px 10px 35px #cac6c6;
      background: #fff;
      /*background-clip——规定背景的绘制区域*/
      /*border-box：背景被裁剪到边框盒*/
      /*padding-box：背景被裁剪到内边距框*/
      /*content-box：背景被裁剪到内容框*/
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
  