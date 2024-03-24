import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import store from './store/index';
import 'font-awesome/css/font-awesome.min.css'
import CryptoJS from 'crypto-js/crypto-js'


/*
封装请求方法,供全局调用
 */
import {postKeyValueRequest} from "./utils/api";
import {postRequest} from "./utils/api";
import {getRequest} from "./utils/api";
import {putRequest} from "./utils/api";
import {deleteRequest} from "./utils/api";

Vue.prototype.postKeyValueRequest=postKeyValueRequest;
Vue.prototype.postRequest=postRequest;
Vue.prototype.getRequest=getRequest;
Vue.prototype.putRequest=putRequest;
Vue.prototype.deleteRequest=deleteRequest;

/*路由前置守卫
to：去哪，from：从哪来，调用next()：通过本次路由请求*/
router.beforeEach((to,from,next)=>{
  if (to.path=="/"||to.path=="/adminlogin"){//首页不需要请求菜单
    next();
  }else if (to.path=="/"||to.path=="/mailLogin"){next();}
  else if (to.path=="/home"&&!window.sessionStorage.getItem('admin')) {
    ElementUI.Message.error({message:"不具有访问权限！"});
    next(from)
  }
  else{
    if (window.sessionStorage.getItem('user')||window.sessionStorage.getItem('admin')){   //登录后才请求菜单
      next();
    }else {//没登录就跳转到登陆页
      //如果先前写了请求路径（to中路径）则记录下来
      ElementUI.Message.error({message:"请登录后访问！"});
      next('/?redirect='+to.path);
    }
  }
})

Vue.config.productionTip = false
Vue.use(ElementUI);

new Vue({
  router,
  store,//这里需要注意
  render: h => h(App)
}).$mount('#app')

// 默认的 KEY 与 iv 如果没有给
const KEY = CryptoJS.enc.Utf8.parse("1234567890123456");
const IV = CryptoJS.enc.Utf8.parse('1234567890123456');
/**
 * AES加密 ：字符串 key iv  返回base64
 */
export function Encrypt(word, keyStr, ivStr) {
  let key = KEY
  let iv = IV

  if (keyStr) {
    key = CryptoJS.enc.Utf8.parse(keyStr);
    iv = CryptoJS.enc.Utf8.parse(ivStr);
  }

  let srcs = CryptoJS.enc.Utf8.parse(word);
  var encrypted = CryptoJS.AES.encrypt(srcs, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.ZeroPadding
  });
  // console.log("-=-=-=-", encrypted.ciphertext)
  return CryptoJS.enc.Base64.stringify(encrypted.ciphertext);

}
/**
 * AES 解密 ：字符串 key iv  返回base64
 *
 */
export function Decrypt(word, keyStr, ivStr) {
  let key = KEY; // 假设KEY是一个先前定义的常量
  let iv = IV; // 假设IV是一个先前定义的常量

  if (keyStr) {
    key = CryptoJS.enc.Utf8.parse(keyStr);
    iv = CryptoJS.enc.Utf8.parse(ivStr);
  }

  // 直接使用Base64编码的字符串进行解密
  var decrypt = CryptoJS.AES.decrypt(word, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.ZeroPadding
  });

  var decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
  return decryptedStr.toString();
}
