import  Vue from 'vue'
import  Vuex from 'vuex'
import {getRequest, postRequest} from "../utils/api";
import SockJS from '../utils/sockjs'
import  '../utils/stomp'
import { Notification } from 'element-ui';

Vue.use(Vuex)

const now = new Date();

const store =  new Vuex.Store({
  state:sessionStorage.getItem('state') ? JSON.parse(sessionStorage.getItem('state')) :{
    routes:[],
    sessions:{},
    users:[],
    currentUser:null,
    currentSession:{username:'group_chat',nickname:'group_chat'},
    currentList:'group_chat',
    filterKey:'',
    stomp:null,
    isDot:{},
    errorImgUrl:"http://20.68.174.190/group1/M00/00/00/J2ypOV7wJkyAAv1fAAANuXp4Wt8303.jpg",
    shotHistory:{},
    conversation:{}
  },
  mutations:{
    initRoutes(state,data){
      state.routes=data;
    },
    changeCurrentSession (state,currentSession) {

      Vue.set(state.isDot,state.currentUser.username+"#"+currentSession.username,false);

      state.currentSession =currentSession;
    },

    changeCurrentList(state,currentList){
      state.currentList=currentList;
    },

    addGroupMessage(state,msg){
      let message=state.sessions['group_chat'];
      if (!message){
        //state.sessions[state.currentHr.username+"#"+msg.to]=[];
        Vue.set(state.sessions,'group_chat',[]);
      }
      state.sessions['group_chat'].push({
        fromId:msg.fromId,
        fromName:msg.fromName,
        fromProfile:msg.fromProfile,
        content:msg.content,
        messageTypeId:msg.messageTypeId,
        createTime: msg.createTime,
        biao: msg.type
      })
      let arr = state.sessions['group_chat']
      arr.forEach((i, index) => {
        if (i.biao == 1) {
          setTimeout(() => {
            arr.splice(index, 1)
          }, 10000)
        }
      })
    },

    addMessage (state,msg) {
      let message=state.sessions[state.currentUser.username+"#"+msg.to];
      if (!message){

        Vue.set(state.sessions,state.currentUser.username+"#"+msg.to,[]);
      }
      state.sessions[state.currentUser.username+"#"+msg.to].push({
        content:msg.content,
        date: new Date(),
        fromNickname:msg.fromNickname,
        messageTypeId:msg.messageTypeId,
        self:!msg.notSelf,
        biao: msg.biaoji
      })
      let arr = state.sessions[state.currentUser.username + "#" + msg.to]
      console.log(arr)
      arr.forEach((i, index) => {
        if (i.biao == 1) {
          setTimeout(() => {
            arr.splice(index, 1)
            console.log(arr)
          }, 10000)
        }
      })
    },

    INIT_DATA (state) {

        getRequest("/groupMsgContent/").then(resp=>{
          if (resp){
            Vue.set(state.sessions,'group_chat',resp);
          }
        })
    },

    INIT_USER(state,data){
      state.users=data;
    },

    GET_USERS(state){
      getRequest("/chat/users").then(resp=>{
        if (resp){
          state.users=resp;
        }
      })
    },
   

  },
  actions:{

    initData (context) {

      context.commit('INIT_DATA')

      context.commit('GET_USERS')
    },

    connect(context){

      console.log("Try to connect to WebSocket service...");
      context.state.stomp=Stomp.over(new SockJS('/ws/ep'));
      context.state.stomp.connect({},success=>{

        context.state.stomp.subscribe("/topic/notification",msg=>{

            Notification.info({
              title: 'system information',
              message: msg.body.substr(5),
              position:"top-right"
            });

            context.commit('GET_USERS');
        });

        context.state.stomp.subscribe("/topic/greetings",msg=>{

          let receiveMsg=JSON.parse(msg.body);

          if (receiveMsg.type == 1) {
            setTimeout(() => {
              postRequest("/groupMsgContent/deleteGroupMsgById", receiveMsg).then(resp => {
              })
            }, 10000)
          }

          if (context.state.currentSession.username!="group_chat"){
            Vue.set(context.state.isDot,context.state.currentUser.username+"#group_chat",true);
          }

          context.commit('addGroupMessage',receiveMsg);
        });

        context.state.stomp.subscribe("/user/queue/robot",msg=>{

          let receiveMsg=JSON.parse(msg.body);

          receiveMsg.notSelf=true;
          receiveMsg.to='机器人';
          receiveMsg.messageTypeId=1;

          context.commit('addMessage',receiveMsg);
        })

        context.state.stomp.subscribe('/user/queue/chat',msg=>{

          let receiveMsg=JSON.parse(msg.body);

          if (!context.state.currentSession||receiveMsg.from!=context.state.currentSession.username){
            Notification.info({
              title:'【'+receiveMsg.fromNickname+'】Sent a message',
              message:receiveMsg.content.length<8?receiveMsg.content:receiveMsg.content.substring(0,8)+"...",
              position:"bottom-right"
            });

            Vue.set(context.state.isDot,context.state.currentUser.username+"#"+receiveMsg.from,true);
          }

          receiveMsg.notSelf=true;

          receiveMsg.to=receiveMsg.from;

          context.commit('addMessage',receiveMsg);
        })
      },error=>{
        Notification.info({
          title: 'system information',
          message: "Unable to establish connection with the server, please try to log in to the system again",
          position:"top-right"
        });
      })
    },

    disconnect(context){
     if (context.state.stomp!=null) {
       context.state.stomp.disconnect();
       console.log("close connection");
     }
    },
  }
})


store.watch(function (state) {
  return state.sessions
},function (val) {
  console.log('CHANGE: ', val);
  localStorage.setItem('chat-session', JSON.stringify(val));
},{
  deep:true
})


export default store;