import Vue from 'vue'
import App from './App.vue'
import router from './router'
import ElementUI from 'element-ui';
import 'element-ui/lib/theme-chalk/index.css';
import store from './store/index';
import 'font-awesome/css/font-awesome.min.css'
import CryptoJS from 'crypto-js/crypto-js'



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


router.beforeEach((to,from,next)=>{
  if (to.path=="/"||to.path=="/adminlogin"){
    next();
  }else if (to.path=="/"||to.path=="/mailLogin"){next();}
  else if (to.path=="/home"&&!window.sessionStorage.getItem('admin')) {
    ElementUI.Message.error({message:"Does not have access rights!"});
    next(from)
  }
  else{
    if (window.sessionStorage.getItem('user')||window.sessionStorage.getItem('admin')){
      next();
    }else {

      ElementUI.Message.error({message:"Please log in to visit!"});
      next('/?redirect='+to.path);
    }
  }
})

Vue.config.productionTip = false
Vue.use(ElementUI);

new Vue({
  router,
  store,
  render: h => h(App)
}).$mount('#app')


export function Encrypt1(word, keyStr, ivStr) {
  let key = keyStr
  let iv = ivStr

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

export function Decrypt1(word, keyStr, ivStr) {
  let key = keyStr; 
  let iv = ivStr; 

  if (keyStr) {
    key = CryptoJS.enc.Utf8.parse(keyStr);
    iv = CryptoJS.enc.Utf8.parse(ivStr);
  }


  var decrypt = CryptoJS.AES.decrypt(word, key, {
    iv: iv,
    mode: CryptoJS.mode.CBC,
    padding: CryptoJS.pad.ZeroPadding
  });

  var decryptedStr = decrypt.toString(CryptoJS.enc.Utf8);
  return decryptedStr.toString();
}
