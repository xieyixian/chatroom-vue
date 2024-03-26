import Vue from 'vue'
import VueRouter from 'vue-router'
import Login from '../views/chat/Login'
import ChatRoom from '../views/chat/ChatRoom'
import AdminLogin from '../views/admin/AdminLogin'
import Home from '../views/admin/Home'
import UserInfo from '../views/admin/UserInfo'
import GroupChatRecord from '../views/admin/GroupChatRecord'
import PrivateChatRecord from '../views/admin/PrivateChatRecord'
import mailLogin from '../views/chat/mailLogin'

Vue.use(VueRouter)

  const routes = [
  {
    path: '/',
    name: 'Login',
    component: Login,
    hidden:true
  },
   {
     path:'/chatroom',
     name:'ChatRoom',
     component:ChatRoom,
     hidden:true
    },
    {
      path:'/adminlogin',
      name:'AdminLogin',
      component:AdminLogin,
      hidden:true
    },
    {
      path:'/home',
      name:'Home',
      component:Home,
      hidden:true
    },
    {
      path:'/mailLogin',
      name:'mailLogin',
      component:mailLogin,
      hidden:true
    },
    {
      path:'/home',
      name:'User Management',
      component:Home,
      iconCls:"fa fa-user",
      children:[{
        path:'/userinfo',
        name:'User Information Management',
        component:UserInfo,
      }]
    },
    {
      path:'/home',
      name:'Chat Record Management',
      iconCls:'fa fa-book',
      component:Home,
      children:[
        {
          path:'/groupChatRecord',
          name:'Group Chat Record Management',
          component:GroupChatRecord
        },
        {
          path:'/privateChatRecord',
          name:'Private Chat Record Management',
          component:PrivateChatRecord
        }
      ]
    }



  // {
  //   path: '/about',
  //   name: 'About',
  //   // route level code-splitting
  //   // this generates a separate chunk (about.[hash].js) for this route
  //   // which is lazy-loaded when the route is visited.
  //   component: () => import(/* webpackChunkName: "about" */ '../views/About.vue')
  // }
]

const originalPush = VueRouter.prototype.push;
VueRouter.prototype.push = function push(location) {
  return originalPush.call(this, location).catch(err => err)
};

const router = new VueRouter({
  routes
})

export default router