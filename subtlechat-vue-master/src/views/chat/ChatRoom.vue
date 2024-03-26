<template>
  <div id="app">
    <div class="toolbar">
      <toolbar></toolbar>
    </div>
    <div class="sidebar">
      <card></card>
      <list></list>
    </div>
    <div class="main">
      <chattitle></chattitle>
      <message></message>
      <usertext></usertext>
    </div>
  </div>
</template>

<script>
  import card from '../../components/chat/card'
  import list from '../../components/chat/list.vue'
  import message from '../../components/chat/message.vue'
  import usertext from '../../components/chat/usertext.vue'
  import toolbar from "../../components/chat/toolbar";
  import chattitle from "../../components/chat/chattitle"

  export default {
    name: 'ChatRoom',
    data () {
      return {

      }
    },
    mounted:function() {

      this.$store.dispatch('initData');

      this.$store.dispatch('connect');

    },
    created () {



      window.addEventListener("beforeunload",()=>{
        sessionStorage.setItem("state",JSON.stringify(this.$store.state))
      })


      let interval = setInterval(()=>{
        let userStr = window.sessionStorage.getItem("user");
        let user = JSON.parse(userStr);
        if(user){
          this.putRequest('/continueLife',user).then(resp=>{
          })
        }else{
          clearInterval(interval)
        }
      },5000)
    },
    components:{
      toolbar,
      card,
      list,
      message,
      usertext,
      chattitle
    }
  }
</script>

<style lang="scss" scoped>
  #app {
    margin: 20px auto;
    width: 900px;
    height: 650px;
    overflow: hidden;
    border-radius: 10px;
    .sidebar, .main ,.toolbar{
      height: 100%;
    }
    .toolbar{
      float: left;
      color: #f4f4f4;
      background-color: #2e3238;
      width: 60px;
    }
    .sidebar {
      float: left;
      color: #000000;
      background-color: #ECEAE8;
      width: 240px;
    }
    .main {
      position: relative;
      overflow: hidden;
      background-color: #eee;
    }
  }
</style>

