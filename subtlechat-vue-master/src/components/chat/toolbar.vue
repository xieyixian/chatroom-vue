<template>
  <div id="toolbar">
    <el-image class="imgProfile"
              :src="user.userProfile"
              :preview-src-list="[user.userProfile]"
              :alt="user.nickname">
      <div slot="error" class="image-slot">
        <i class="el-icon-picture-outline"></i>
      </div>
    </el-image>
    <div id="btnBar">
      <div class="topBtnBar">
        <el-tooltip  class="item" effect="dark" content="Enter group chat" placement="right">
        <el-button @click="chooseChatList('group_chat')" class="toolBtn" size="small"><i class="fa fa-comments fa-2x" aria-hidden="true"></i></el-button>
        </el-tooltip>
        <el-tooltip class="item" effect="dark" content="User List" placement="right">
        <el-button @click="chooseChatList('私聊')" class="toolBtn" size="small"><i class="fa el-icon-user-solid fa-2x" aria-hidden="true"></i></el-button>
        </el-tooltip>

      </div>
      <div class="bottomBtnBar">

        <el-tooltip class="item" effect="dark" content="exit" placement="right">
        <el-button @click="exitSystem" class="toolBtn" size="small"><i class="fa fa-sign-out fa-2x" aria-hidden="true"></i></el-button>
        </el-tooltip>
      </div>
    </div>

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
      exitSystem() {
      this.$confirm('Are you sure you want to exit the system?', 'System Prompt', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        this.getRequest("/logout");
        sessionStorage.removeItem("user");
        // Clear the state saved in SessionStorage
        if (sessionStorage.getItem("state")){
          sessionStorage.removeItem("state");
        }
        // Close the connection
        this.$store.dispatch("disconnect");
        this.$router.replace("/");
      }).catch(() => {
        this.$message({
          type: 'info',
          message: 'Operation cancelled'
        });
      });
     },

      chooseChatList(listName){
        this.$store.commit("changeCurrentList",listName);
      },

      showFeedbackDialog(){
        this.feedBackContent='';
        this.feedBackDialogVisible=true;
      },

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

      clearChatHistory() {
      this.$confirm('This action will permanently delete local chat history (group chat records will be restored upon next login). Continue?', 'Tip', {
        confirmButtonText: 'OK',
        cancelButtonText: 'Cancel',
        type: 'warning'
      }).then(() => {
        // Clear local chat records in localStorage
        if (localStorage.getItem("chat-session")){
          localStorage.removeItem("chat-session");
        }
        // Clear records saved in Vuex
        this.$store.state.sessionStorage = {};
        // Clear state saved in SessionStorage
        if (sessionStorage.getItem("state")){
          sessionStorage.removeItem("state");
        }
        this.$message({
          type: 'success',
          message: 'Deleted successfully'
        });
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
