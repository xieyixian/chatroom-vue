<template>
  <el-container>
    <el-header >
      <el-button @click="gotoAdminLogin" icon="el-icon-d-arrow-right" style="float: right;border: none" >Admin Login</el-button>
    </el-header>
    <el-main>
      <div class="loginContainer">
        <el-form ref="loginForm" :rules="rules" :model="loginForm" label-width="80px">
          <h3 class="loginTitle">SubtleChat~</h3>
          <el-form-item label="Username:" prop="username">
            <el-input type="text" v-model="loginForm.username" auto-complete="off" placeholder="Please enter your username"></el-input>
          </el-form-item>
          <el-form-item label="Password:" prop="password">
            <el-input type="password" v-model="loginForm.password" auto-complete="off" placeholder="Please enter your password"></el-input>
          </el-form-item>
          <el-form-item label="verify code:" prop="code">
            <el-input type="text" @keydown.enter.native="submitLogin" v-model="loginForm.code" auto-complete="off" placeholder="Please enter the verification code" style="width:150px;"></el-input>
            <img :src="verifyCode" title="点击切换验证码" @click="changeverifyCode" />
          </el-form-item>
          <el-checkbox v-model="checked" class="loginRemember"></el-checkbox><span> Remember me for a week</span>
          <div>
            <el-button @click="showRegistryDialog" style="width:45% ;margin-right: 15px">Register</el-button>
            <el-button type="primary" style="width:45% ;" @click="submitLogin" v-loading.fullscreen.lock="fullscreenLoading">Log In</el-button>
          </div>
        </el-form>
      </div>
    </el-main>
    <el-dialog title="New user registration" :before-close="closeRegisterDialog" :visible.sync="registerDialogVisible" width="30%">
      <el-form :model="registerForm" status-icon :rules="registerRules" ref="registerForm" >
        <el-form-item label="Nickname:" :label-width="formLabelWidth" prop="nickname">
          <el-input v-model=" registerForm.nickname" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="username：" :label-width="formLabelWidth" prop="username">
            <el-input v-model="registerForm.username" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="password：" :label-width="formLabelWidth" prop="password">
          <el-input type="password" v-model="registerForm.password" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="Confirm ：" :label-width="formLabelWidth" prop="checkPass">
          <el-input type="password" v-model="registerForm.checkPass" autocomplete="off"></el-input>
        </el-form-item>
        <el-form-item label="profile photo：" :label-width="formLabelWidth">
          <el-upload
                  action="/ossFileUpload?module=group-chat"
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
            <div slot="tip" class="el-upload__tip">Images must be under 4MB (You can use the default avatar!)</div>
          </el-upload>
        </el-form-item>
      </el-form>
      <div slot="footer" class="dialog-footer">
        <el-button type="primary" @click="submitRegisterForm('registerForm') " style="width: 100%">Register</el-button>
      </div>
    </el-dialog>
  </el-container>


</template>

<script>

  export default {
    name: "Login",
    data(){
      var validateNickname = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('Please enter a nickname'));
        }
        //检查昵称是否重复
          this.getRequest("user/checkNickname?nickname="+value).then(resp=>{
            if (resp!=0){
              callback(new Error("The nickname has been registered"))
            } else {
              callback();
            }
          })
      };
      var validateUsername = (rule, value, callback) => {
        if (value === '') {
          callback(new Error('Please enter your username'));
        }
        //检查用户名是否重复
        this.getRequest("/user/checkUsername?username="+value).then(resp=>{
            if (resp!=0){
              callback(new Error('The user name has been registered'));
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
          callback(new Error('Please re-enter your password'));
        } else if (value !== this.registerForm.password) {
          callback(new Error('Passwords do not match!'));
        } else {
          callback();
        }
      };
      return{
        loginForm:{
           username:'',
           password:'',
           code:''
        },
        verifyCode:'/verifyCode',
        checked:true,
        rules: {
          username:[{required:true,message:'Please enter your username',trigger:'blur'}],
          password:[{required:true,message: 'Please enter password',trigger:'blur'}],
          code:[{required:true,message: 'Please enter the verification code',trigger:'blur'}]
        },
        fullscreenLoading:false,
        //注册表单相关
        registerDialogVisible:false,
        formLabelWidth: '120px',
        registerForm:{
          nickname:'',
          username:'',
          password:'',
          checkPass:'',
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
    methods:{
      submitLogin(){
        this.$refs.loginForm.validate((valid) => {
          if (valid) {
            this.fullscreenLoading=true;
            this.postKeyValueRequest('/doLogin',this.loginForm).then(resp=>{
              setTimeout(()=>{
                this.fullscreenLoading=false;
              },1000);
              if (resp){
                //保存当前用户到vuex
                this.$store.state.currentUser=resp.obj;
                //保存登录用户到sessionStorage中
                window.sessionStorage.setItem("user",JSON.stringify(resp.obj));
                let path=this.$route.query.redirect;
                this.$router.replace((path=='/'||path==undefined)?"/chatroom":path);
              }else {
                this.changeverifyCode();
              }
            })
          } else {
            this.$message.error("Username, password and verification code can not be empty!");
            return false;
          }
        });
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
          this.$message.error('Upload profile picture size can not exceed 4MB!');
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
        console.log("Image url is："+this.registerForm.userProfile);
      },
      // 图片上传失败
      imgError(err, file, fileList){
        this.$message.error("Upload failure");
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
              }
            })
          } else {
            this.$message.error("Please fill in the information correctly!");
            console.log('error submit!!');
            return false;
          }
        });
      },

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
