<template>
  <div id="list">
  	<ul v-if="currentList=='group_chat'">

			<p style="padding: 2px 4px;height: 20px">Group Chat</p>
			<li :class="{ active: currentSession?'group_chat'== currentSession.username:false }"
					v-on:click="changeCurrentSession(chatObj)">
				<img class="avatar" src="http://20.68.174.190/group1/M00/00/00/CgAABGYBrEqAb3LXAAALY52h4i8878.png">
				<el-badge :is-dot="isDot[user.username+'#group_chat']"><p class="name">GroupChat</p></el-badge>
			</li>
			</ul>


		<el-scrollbar      wrap-class="userList" wrap-style="height:600px;"
											  view-style="height:100%;" :native="false">
		<ul v-if="currentList=='私聊'" >
			<p style="padding: 2px 4px;height: 20px">User List</p>
  		<li v-for="item in users" :class="{ active: currentSession?item.username === currentSession.username:false }"
					v-on:click="changeCurrentSession(item)"><!--   :class="[item.id === currentSession ? 'active':'']" -->
				<div style="display: flex;justify-content: space-between">
					<div>
						<el-badge :is-dot="isDot[user.username+'#'+item.username]" style="">
							<el-image class="avatar"
												:preview-src-list="[item.userProfile]"
												:src="item.userProfile"
												:alt="item.nickname">
								<div slot="error" class="image-slot">
									<i class="el-icon-picture-outline"></i>
								</div>
							</el-image>
						</el-badge>
						<p class="name">{{item.nickname}}</p>
					</div>
					<div>
					<el-badge :value="item.userStateId==1?'Online':'Offline'" :type="item.userStateId==1?'danger':'info'"></el-badge>
					</div>
				</div>
  		</li>
  	</ul>
		</el-scrollbar>
  </div>
</template>

<script>
import {mapState} from 'vuex'

export default {
  name: 'list',
  data () {
    return {
			user:this.$store.state.currentUser,
			chatObj:{username:'group_chat',nickname:'group_chat'},
			robotObj:{
				username:'机器人',
				nickname:'机器人',
			  userProfile:''
			}
    }
  },
  computed: mapState([

	'users',
  'currentSession',
	'isDot',
	'currentList'
	]),
  methods:{

	changeCurrentSession(currentSession) {
    this.$store.commit('changeCurrentSession', currentSession);

    if (currentSession.username !== 'group_chat' && currentSession.username !== '机器人') {
      this.initPrivateChat(currentSession);
    }
  },
  initPrivateChat(currentSession) {

      const payload = {
        username: this.$store.state.currentUser.username,
        otherUsername: currentSession.username 
      };
	  
	  console.log("aaaaaaaaaaacccccc:", this.$store.state.currentUser);
	   console.log("aaaaaaaaaaacccccc:", this.user.username);
	  console.log("aaaaaaaaaaacccccc:", this.user.username);
	  console.log("aaaaaaaaaaacccccc:", currentSession.username);

      this.postRequest("/userchat/conversations/join", payload)
        .then(response => {
          this.$store.state.conversation = response.conversation;
			console.log(response.conversation.conversationId);
			const userConversationId = response.conversation.conversationId;

		this.postRequest("/getAESKey?userConversationId=" + userConversationId)
			.then(response1 => {
				sessionStorage.setItem("AESKey",response1)
    	});


        })
        .catch(error => {
    
          console.error("Error initializing private chat:", error);
          if (error.response) {
 
            console.log("Error response data:", error.response.data);
            console.log("Error response status:", error.response.status);
          } else if (error.request) {
   
            console.log("No response received:", error.request);
          } else {
 
            console.log("Error setting up request:", error.message);
          }
        }
		
		
		);
		

		// this.getRequest("/userchat/conversations/41/messages").then(resp=>{
    //       if (resp){
		// 	console.log("11112111Error response data:", resp);
    //         //Vue.set(state.sessions,'group_chat',resp);
    //       }
    //     })
    }


  }
}
</script>

<style lang="scss" scoped>
#list {
	ul{
		margin-left: 0px;
		padding-left: 0px;
		margin-left: 2px;
	}
	li {
		padding-top: 14px;
		padding-bottom: 14px;
		//padding-right: 40px;
		//border-bottom: 1px solid #292C33;
		list-style: none;
		cursor: pointer;
		&:hover {
			background-color: #D8D6D6;
		}
	}
  li.active {
			background-color: #C8C6C6;
	}
	.avatar {
		border-radius: 2px;
		width: 30px;
		height: 30px;
		vertical-align: middle;
	}
	.name {
		display: inline-block;
		margin-left: 15px;
		margin-top: 0px;
		margin-bottom: 0px;
	}
	.stateItem {
		/*position: absolute;*/
		/*left: 160px;*/
		//margin-left: 100px;
		//margin-right: 10px;
	}
	.userList{
		max-height: 600px;
	}
	.el-scrollbar__wrap.default-scrollbar__wrap {
		overflow-x: auto;
	}
}
</style>
