<template>
<div class="chatTitle">
  <span class="titleName" v-if="this.$store.state.currentSession.nickname != 'group_chat'">{{ this.$store.state.currentSession.nickname ? this.$store.state.currentSession.nickname : '' }}</span>
  <span class="titleName" v-else>GroupChat</span>

  
</div>

</template>

<script>
  export default {
    name: "chattitle"
  }
</script>

<style scoped>
  .chatTitle{
    height: 50px;
    width: 100%;
    font-size: 20px;
    display: flex;
    justify-content: space-between;
    border-bottom: 0.5px solid #c7d2db;
  }
  .moreBtn{
    background-color: #eee;
    border: 0;
    height: 40px;
  }
  .titleName{
    margin: 10px 20px;
  }

</style>
