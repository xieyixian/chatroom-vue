import axios from 'axios';
import {Message} from "element-ui";
import router from '../router'


/*axios全局响应拦截*/
axios.interceptors.response.use(success=>{
  if (success.status&&success.status==200&&success.data.status==500){//请求成功，但处理出现其他错误
    Message.error({message: 'Login failed'})
    return
  }
  //请求成功且服务器处理无错误
  if (success.data.msg){
    Message.success({message: 'Login successful'});
  }
  return success.data;
},error => {
  if (error.response.status==504) {//	充当网关或代理的服务器，未及时从远端服务器获取请求
    Message.error({message:'Unable to find server'})
  }else if(error.response.status==403){	//服务器理解请求客户端的请求，但是拒绝执行此请求
    Message.error({message:'Insufficient permissions, please contact the administrator'})
  }else if (error.response.status==401){//请求要求用户的身份认证
    Message.error({message:'Not logged in yet, please log in'});
    router.replace("/");//跳转到登陆页
  }else if (error.response.status==404){
    Message.error({message:'The server is unable to find resources based on the client\'s request'})
  } else if (error.response.status==500){
    Message.error({message:'Internal server error, unable to complete request'})
  } else {
    if (error.response.data){
      Message.error({message:error.response.data.msg})
    }
    else {
      Message.error({message:'unknown error!'})
    }
  }
  return;
})

let base='';

/*
登录请求方法，与服务端Spring Security的登录接口对接
 */
export const postKeyValueRequest=(url,params)=>{
  return axios({
    method:'post',
    url:`${base}${url}`,
    data:params,
    transformRequest:[function (data) {//处理请求的数据格式
      //console.log(data);
      let ret='';
      for (let i in data){
        ret+=encodeURIComponent(i)+'='+encodeURIComponent(data[i])+'&'
      }
     // console.log(ret);
      return ret;
    }],
    headers:{
      'Content-Type':'application/x-www-form-urlencoded'
    }
  });
}
/*
封装“增加”请求方法——post
 */
export const postRequest=(url,params)=>{
  return axios({
    method:'post',
    url:`${base}${url}`,
    data:params
  });
}
/*
封装“修改”请求方法——put
 */
export const putRequest=(url,params)=>{
  return axios({
    method:'put',
    url:`${base}${url}`,
    data:params
  });
}
/*
封装“查询”请求方法——get
 */
export const getRequest=(url,params)=>{
  return axios({
    method:'get',
    url:`${base}${url}`,
    data:params
  });
}
/*
封装“删除”请求方法——get
 */
export const deleteRequest=(url,params)=>{
  return axios({
    method:'delete',
    url:`${base}${url}`,
    data:params
  });
}
