<template>
  <el-container>
    <el-header >
      <p class="text">
      </p>
      <el-button @click="gotoAdminLogin" icon="el-icon-d-arrow-right" style="float: right;border: none" >管理端登录</el-button>
    </el-header>
    <el-main>
      <div class="loginContainer">
        <el-form ref="loginForm" :rules="rules" :model="loginForm" label-width="80px">
          <h3 class="loginTitle">微言SubtleChat~</h3>
          <el-form-item label="用户名:" prop="username">
            <el-input type="text" v-model="loginForm.username" auto-complete="off" placeholder="请输入用户名"></el-input>
          </el-form-item>
          <el-form-item label="密码:" prop="password">
            <el-input type="password"  v-model="loginForm.password" auto-complete="off" placeholder="请输入密码"></el-input>
          </el-form-item>
          <el-form-item label="验证码:" prop="code">
            <el-input type="text" @keydown.enter.native="submitLogin" v-model="loginForm.code" auto-complete="off" placeholder="请输入验证码" style="width:150px;"></el-input>
            <img :src="verifyCode" title="点击切换验证码" @click="changeverifyCode" />
          </el-form-item>
          <el-checkbox v-model="checked" class="loginRemember"></el-checkbox><span> 记住我一周</span>
          <div>
            <el-button @click="showRegistryDialog" style="width:45% ;margin-right: 15px">注册</el-button>
            <el-button type="primary" style="width:45% ;" @click="submitLogin"  v-loading.fullscreen.lock="fullscreenLoading">登录</el-button>
          </div>
        </el-form>
      </div>
    </el-main>
    <el-dialog title="新用户注册" :before-close="closeRegisterDialog" :visible.sync="registerDialogVisible" width="30%">
      <el-form :model="registerForm" status-icon :rules="registerRules" ref="registerForm" >
        <el-form-item label="用户昵称：" :label-width="formLabelWidth" prop="nickname">
          <el-input v-model=" registerForm.nickname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="登录用户名：" :label-width="formLabelWidth" prop="username">
            <el-input v-model="registerForm.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="密码：" :label-width="formLabelWidth" prop="password">
          <el-input type="password" v-model="registerForm.password" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="确认密码：" :label-width="formLabelWidth" prop="checkPass">
          <el-input type="password" v-model="registerForm.checkPass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="用户头像：" :label-width="formLabelWidth">
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
            <div slot="tip" class="el-upload__tip">只能上传不超过4MB的图片(可使用默认头像！)</div>
          </el-upload>
        </el-form-item>
        <el-form-item label="邮箱地址:" prop="email">
          <el-input type="text"  v-model="registerForm.email" auto-complete="off" placeholder="请输入邮箱" style="width: 120px;margin-right: 10px" ></el-input>
          <el-button @click="getMailVerifyCode" :disabled="getCodeEnable"  size="mini">{{getCodeBtnText}}</el-button>
        </el-form-item>

        <el-form-item label="验证码:" prop="mailCode">
          <el-input type="text"  v-model="registerForm.mailCode" auto-complete="off" placeholder="请输入验证码" style="width: 120px;margin-right: 10px" ></el-input>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRegisterForm('registerForm') " style="width: 100%">注册</el-button>
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
          callback(new Error('请输入昵称'));
        }
        //检查昵称是否重复
          this.getRequest("user/checkNickname?nickname="+value).then(resp=>{
            if (resp!=0){
              callback(new Error("该昵称已被注册"))
            } else {
              callback();
            }
          })
      };
      var validateUsername = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请输入用户名'));
        }
        //检查用户名是否重复
        this.getRequest("/user/checkUsername?username="+value).then(resp=>{
            if (resp!=0){
              callback(new Error('该用户名已被注册'));
            }
            else {
              callback();
            }
          })

      };
      var validatePass = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请输入密码'));
        } else {
          if (this.registerForm.checkPass !== '') {
            this.$refs.registerForm.validateField('checkPass');
          }
          callback();
        }
      };
      var validatePass2 = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('请再次输入密码'));
        } else if (value !== this.registerForm.password) {
          callback(new Error('两次输入密码不一致!'));
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
          username:[{required:true,message:'请输入用户名',trigger:'blur'}],
          password:[{required:true,message: '请输入密码',trigger:'blur'}],
          code:[{required:true,message: '请输入验证码',trigger:'blur'}],
          email:[{required:true,message: '请输入邮箱',trigger:'blur'}],
          mailCode:[{required:true,message: '请输入验证码',trigger:'blur'}]
        },
        fullscreenLoading:false,
        //注册表单相关
        registerDialogVisible:false,
        getCodeBtnText:'获取邮箱验证码',
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
        //上传的文件信息列表
        fileList:[],
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
          })
    },
    methods:{

        encryptData() {
          const encryptor = new JSEncrypt();
          let storedKey = sessionStorage.getItem('publicKey');
          storedKey = storedKey.substring(1, storedKey.length - 1);
          encryptor.setPublicKey(storedKey);

          sessionStorage.setItem("encryptedUsername",this.loginForm.username);
          // 加密数据
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
      body: JSON.stringify({ /* 你需要发送的数据 */ })
    });
    
    const data = await response.json();
    console.log("111111111111111:", data.prediction);
    return data.prediction; // 直接返回预测值
  } catch (error) {
    console.error('IP check request failed:', error);
    throw new Error('IP检查请求失败');
  }
},

async submitLogin() {
  // 获取IP检查的结果
  const ipcheck = await this.checkIPAddress();

  if (ipcheck !== 2) {
    let message;

    if (ipcheck === 0) {
      message = '你的IP检查通过，是否继续登录?';
    } else {
      message = '你的IP可能存在威胁，是否继续登录?';
    }
     console.log('message:', message);
    try {
      await this.$confirm(message, '确认', {
        confirmButtonText: '确定',
        cancelButtonText: '取消',
        type: 'warning'
      });

      // 弹窗后的逻辑处理
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
              // 成功逻辑处理
              this.$store.state.currentUser = resp.obj.user;
              sessionStorage.setItem("encryptedPassword", this.loginForm.password);
              sessionStorage.setItem("code", this.loginForm.code);
              this.$router.replace("/mailLogin");
            } else {
              // 登录失败逻辑处理
              this.loginForm.username = '';
              this.loginForm.password = '';
              this.changeverifyCode();
            }
          } catch (error) {
            this.fullscreenLoading = false;
            console.error('登录请求失败:', error);
          }
        } else {
          this.$message.error("用户名,密码和验证码都不能为空！");
        }
      });
    } catch (error) {
      // 用户取消弹窗
      console.log('用户取消了操作');
    }
  } else {
    // 黑名单用户的处理逻辑
    console.log("黑名单不能登录系统:", ipcheck);
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
      /**
       *       图片上传的方法
       */
      //上传前
      beforeAvatarUpload(file) {
        let isLt4M = file.size / 1024 / 1024 < 4;

        if (!isLt4M) {
          this.$message.error('上传头像图片大小不能超过 4MB!');
        }
        return isLt4M;
      },
      // 上传中
      onProgress(event, file, fileList){
        this.uploadDisabled = true;
      },
      // 图片上传成功
      imgSuccess(response, file, fileList) {
        this.uploadDisabled = true;
        this.registerForm.userProfile=response;//将返回的路径给表单的头像属性
        console.log("图片url为："+this.registerForm.userProfile);
      },
      // 图片上传失败
      imgError(err, file, fileList){
        this.$message.error("上传失败");
        this.uploadDisabled = false;
      },
      //移除图片
      imgRemove(file,fileList){
        this.uploadDisabled = false;
      },
      closeRegisterDialog(done){
        this.registerForm={//清空表单
          nickname:'',
          username:'',
          password:'',
          checkPass:'',
          userProfile:'',
        };
        //this.$refs.upload.clearFiles();//清除上传组件的图片
        done();//关闭对话框
      },
      //提交注册操作
      submitRegisterForm(formName) {
        this.$refs[formName].validate((valid) => {
          if (valid) {
            this.postRequest("/user/register",this.registerForm).then(resp=>{
              if (resp){
                this.registerDialogVisible=false;
                location.reload();//刷新页面，清空注册界面的上传组件中的图片
              }else{
                location.reload();
              }
            })
          } else {
            this.$message.error("请正确填写信息！");
            console.log('error submit!!');
            return false;
          }
        });
      },
      // 获取邮箱验证码
      async getMailVerifyCode() {
        const email = this.registerForm.email;
        // 发送POST请求
        try {
          const response = await axios.post('/user/mailVerifyCode', { email: email });

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