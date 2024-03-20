<template>
  <div id="toolbar">
    <!-- Image and Profile -->
    <el-image class="imgProfile"
              :src="user.userProfile"
              :preview-src-list="[user.userProfile]"
              :alt="user.nickname">
      <div slot="error" class="image-slot">
        <i class="el-icon-picture-outline"></i>
      </div>
    </el-image>
    <!-- Button Bar -->
    <div id="btnBar">
      <div class="topBtnBar">
        <!-- Group Chat Button -->
        <el-tooltip class="item" effect="dark" content="Enter Group Chat" placement="right">
          <el-button @click="chooseChatList('Group Chat')" class="toolBtn" size="small">
            <i class="fa fa-comments fa-2x" aria-hidden="true"></i>
          </el-button>
        </el-tooltip>
        <!-- User List Button -->
        <el-tooltip class="item" effect="dark" content="User List" placement="right">
          <el-button @click="chooseChatList('Private Chat')" class="toolBtn" size="small">
            <i class="fa fa-address-book-o fa-2x" aria-hidden="true"></i>
          </el-button>
        </el-tooltip>
        <!-- Chat with Bot Button -->
        <el-tooltip class="item" effect="dark" content="Chat with Bot" placement="right">
          <el-button @click="chooseChatList('Bot')" class="toolBtn" size="small">
            <i class="fa fa-android fa-2x" aria-hidden="true"></i>
          </el-button>
        </el-tooltip>
      </div>
      <div class="bottomBtnBar">
        <!-- User Center Button -->
        <el-tooltip class="item" effect="dark" content="User Center" placement="right">
          <el-button class="toolBtn" size="small">
            <i class="fa fa-user fa-2x" aria-hidden="true"></i>
          </el-button>
        </el-tooltip>
        <!-- More Options Button -->
        <el-tooltip class="item" effect="dark" content="More" placement="right">
          <el-popover placement="right" width="180" trigger="click" popper-class="moreListPopoverClass">
            <ul id="moreList">
              <li @click="showFeedbackDialog">Feedback</li>
              <li>Report</li>
              <li @click="clearChatHistory">Clear Chat History</li>
            </ul>
            <el-button slot="reference" class="toolBtn" size="small">
              <i class="fa fa-bars fa-2x" aria-hidden="true"></i>
            </el-button>
          </el-popover>
        </el-tooltip>
        <!-- Logout Button -->
        <el-tooltip class="item" effect="dark" content="Logout" placement="right">
          <el-button @click="exitSystem" class="toolBtn" size="small">
            <i class="fa fa-sign-out fa-2x" aria-hidden="true"></i>
          </el-button>
        </el-tooltip>
      </div>
    </div>
    <!-- Feedback Dialog -->
    <el-dialog title="Feedback" :visible.sync="feedBackDialogVisible" class="feedbackDialog">
      <textarea class="feedbackInput" v-model="feedBackContent"></textarea>
      <span slot="footer" class="dialog-footer">
        <el-button type="primary" @click="handleFeedbackSend">Confirm</el-button>
        <el-button @click="feedBackDialogVisible = false">Cancel</el-button>
      </span>
    </el-dialog>
  </div>
</template>
<script>
  export default {
    name: "toolbar",
    data(){
      return{
        user:JSON.parse(window.sessionStorage.getItem('user')),
        feedBackDialogVisible:false,
        feedBackContent:'',
      }
    },
    methods:{
      // Exiting the system
      exitSystem(){
        this.$confirm('Are you sure you want to exit the system?', 'System Prompt', {
          confirmButtonText: 'Confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          this.getRequest("/logout");
          sessionStorage.removeItem("user");
          //清除SessionStorage中保存的state
          if (sessionStorage.getItem("state")){
            sessionStorage.removeItem("state");
          }
          //关闭连接
          this.$store.dispatch("disconnect");
          this.$router.replace("/");
        }).catch(() => {
          this.$message({
            type: 'info',
            message: 'Operation cancelled'
          });
        });
      },
      //选择聊天列表
      chooseChatList(listName){
        this.$store.commit("changeCurrentList",listName);
      },
      //打开意见反馈对话框
      showFeedbackDialog(){
        this.feedBackContent='';
        this.feedBackDialogVisible=true;
      },
      //处理反馈消息邮件发送
      handleFeedbackSend(){
        let msgObj={};
        msgObj.userId=this.user.id;
        msgObj.nickname=this.user.nickname;
        msgObj.username=this.user.username;
        msgObj.content=this.feedBackContent;
        console.log(msgObj)
        this.postRequest("/mail/feedback",msgObj).then(resp=>{
          if (resp) {
            this.feedBackDialogVisible = false;
          }
        })
      },
      //清空聊天记录
      clearChatHistory(){
        this.$confirm('This action will permanently delete the local chat history (group chat history will be restored the next time you log in). Do you want to continue?', 'Tips', {
          confirmButtonText: 'confirm',
          cancelButtonText: 'Cancel',
          type: 'warning'
        }).then(() => {
          //清除本地的localStorage中的聊天记录
          if (localStorage.getItem("chat-session")){
            localStorage.removeItem("chat-session");
          }
          //清除Vuex中保存的记录
          this.$store.state.sessionStorage={};
          //清除SessionStorage中保存的state
          if (sessionStorage.getItem("state")){
            sessionStorage.removeItem("state");
          }
          this.$message({
            type: 'success',
            message: 'Successfully deleted'
          });
        }).catch(() => {
          this.$message({
            type: 'info',
            message: 'undeleted'
          });
        });
      }
    }
  }
</script>

<style lang="scss" scoped>
  #toolbar{
    width: 100%;
    height: 100%;
    #btnBar{
      width: 100%;
      height: 82%;
      display: flex;
      flex-direction: column;
      justify-content: space-between;
    }
    .imgProfile{
      width: 40px;
      height: 40px;
      horiz-align: center;
      margin: 25px 10px;
    }
    .toolBtn{
      background-color: #2e3238;
      border: 0;
      margin: 5px 5px;
    }
    .feedbackDialog{
      width: 1000px;
      height: 800px;
      margin: 10px auto;
      //background-color: #ECEAE8;
    }
    .feedbackInput{
      width: 450px;
      height: 200px;
      resize: none;
      padding: 0;
      margin: 0;
    }
  }


  #moreList{
    margin: 0px;
    padding: 0px;
    background-color: #2e3238;
    li {
      padding-top: 14px;
      padding-bottom: 14px;
      padding-left: 5px;
      //padding-right: 40px;
      //border-bottom: 1px solid #292C33;
      list-style: none;
      cursor: pointer;
      &:hover {
        background-color: #abaaaa;
      }
    }
  }

</style>
<style lang="scss">
  /* el-popover是和app同级的，所以scoped的局部属性设置了无效 */
  /* 需要设置全局style */
  .el-popover.moreListPopoverClass{
    height:150px;
    width:150px;
   // margin: 0px;
    margin-left: 10px;
    padding: 0px;
    overflow-x: hidden;
    overflow-y: hidden;
    background-color:#2e3238;
    border:none;
  }
</style>
