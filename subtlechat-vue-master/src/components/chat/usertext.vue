<template>
  <div id="uesrtext">
    <div>
      <el-popover placement="top-start" width="400" trigger="click" class="emoBox">
        <div class="emotionList">
          <a href="javascript:void(0);" @click="getEmo(index)" v-for="(item,index) in faceList" :key="index" class="emotionItem">{{item}}</a>
        </div>
        <el-button id="emojiBtn" class="emotionSelect" slot="reference">
          <i class="fa fa-smile-o" aria-hidden="true"></i>
        </el-button>
      </el-popover>
      <el-upload
              class="upload-btn"
              action="/file"
              :before-upload="beforeAvatarUpload"
              :on-success="imgSuccess"
              :on-error="imgError"
              :show-file-list="false"
              accept=".jpg,.jpeg,.png,.JPG,JPEG,.PNG,.gif,.GIF"
              >
        <el-button id="uploadImgBtn" icon="el-icon-picture-outline"></el-button>
      </el-upload>
      <el-button id="steganographyBtn" slot="reference" class="no-border" @click="showDialog">
        <i class="fa" aria-hidden="true">隐写图片</i>
      </el-button>
      <el-dialog :visible.sync="dialogVisible" title="隐写图片" width="80%" @close="closeDialog" top>
        <div class="emotionList">
          <div style="height: 30px;line-height: 30px;width: 100%">选择文件：<input type='file' id='file' @change="importImage"/></div>
          <hr />
          <div style="width: 100%;">
            <span style="width: 100%">图片预览</span>
            <canvas id='canvas' style="width: 100%;"></canvas>
          </div>
          <hr />
          <!--要隐写的信息-->
          <div style="width: 100%;margin: 10px 0;">
            隐写信息：<textarea id='msgContent' cols="100" rows="1" style="border-color: #e6a23c"></textarea>
            <button id='encode' class='submit' @click="encode">隐写</button>
          </div>
          <hr />
          <!--隐写后的图片-->
          <div style="width: 100%;">
            隐写图片：<img id='output' style="width: 100%;"/>
          </div>
          <hr />
          <div>
            <button id='decode' @click="decode">从隐写图片读取信息</button>
            读出的隐写内容：<span id='messageDecoded'></span>
          </div>
          <hr />
          <!--解密出的信息-->
          <div id="footer" slot="footer">
            <span style="display: inline-block;float: right">
              <button id='close' @click="closeDialog">取消</button>
              <button id='selectImg' @click="selectImg">上传</button>
            </span>
          </div>
        </div>
      </el-dialog>

      <el-button id="unSteganographyBtn" slot="reference" class="no-border" @click="parseShowDialog">
        <i class="fa" aria-hidden="true">解析隐写图片</i>
      </el-button>
      <el-dialog :visible.sync="parseDialogVisible" title="解析隐写图片" width="80%" @close="parseCloseDialog" top>
        <div class="emotionList">
          <div style="height: 30px;line-height: 30px;width: 100%">
            选择文件：<input type='file' id='parseFile' @change="parseImportImage"/></div>
          <hr />
          <div style="width: 100%;">
            <span style="width: 100%">图片预览</span>
            <canvas id='parseCanvas' style="width: 100%;"></canvas>
          </div>
          <hr />
          <div>
            <button id='parseDecode' @click="parseDecode">从隐写图片读取信息</button>
            读出的隐写内容：<span id='parseMessageDecoded'></span>
          </div>
          <hr />
          <!--解密出的信息-->
          <div id="parseFooter" slot="footer">
            <span style="display: inline-block;float: right">
              <button id='parseClose' @click="parseCloseDialog">取消</button>
            </span>
          </div>
        </div>
      </el-dialog>
    </div>
    <textarea id="textarea" placeholder="按 Ctrl + Enter 发送" v-model="content" v-on:keyup="addMessage">
    </textarea>
    <el-button id="sendBtn" type="primary" size="mini" @click="addMessageByClick" >发送(S)</el-button>
  </div>
</template>

<script>
import {mapState} from 'vuex';

const appData=require("../../utils/emoji.json")//引入存放emoji表情的json文件

export default {
  name: 'uesrtext',
  data () {
    return {
      faceList:[],//表情包数据
      content:'',
      dialogVisible: false,
      parseDialogVisible: false,
      isLimited: false
    }
  },
  mounted(){
    for (let i in appData){//读取json文件保存数据给数组
      this.faceList.push(appData[i].char);
    }
  },
  computed:mapState([
    'sessions',
    'currentSession'
  ]),
  methods: {
    addMessageByClick(){
      if(!this.content || this.content.match(/^[ ]*$/)) {
        this.$message({
          showClose: true,
          message: '不能发送空白信息'
        });
        return;
      }
      if(this.isLimited){
        this.$message({
          showClose: true,
          message: '操作太频繁，稍后再试'
        });
        return;
      }
      this.isLimited = true;
      let msgObj=new Object();
      msgObj.content=this.content;
      msgObj.messageText=this.content;
      msgObj.messageTypeId=1;
      msgObj.from=this.$store.state.currentUser.username;

      console.log(this.$store.state);
      console.log(this.$store.state.conversation.conversationId);
      console.log(JSON.parse(JSON.stringify(this.$store.state.conversation)));
      console.log('currentUserId:',this.$store.state.currentUser.id);
      //发送群聊消息
      if (this.currentSession.username=="群聊"){
        console.log(this.content);
        msgObj.fromId = this.$store.state.currentUser.id;
        this.$store.state.stomp.send("/ws/groupChat",{},JSON.stringify(msgObj));
      }
      //给机器人发送消息
      if (this.currentSession.username=="机器人"){
        msgObj.fromNickname=this.$store.state.currentUser.nickname;
        msgObj.to='机器人';
        this.$store.state.stomp.send("/ws/robotChat",{},JSON.stringify(msgObj));
        //保存该条记录到session
        this.$store.commit('addMessage',msgObj);
      }
      //发送私聊消息
      else{
        // msgObj.from=this.$store.state.currentUser.username;
        msgObj.fromNickname=this.$store.state.currentUser.nickname;
        msgObj.to=this.currentSession.username;
        msgObj.senderUsername=this.$store.state.currentUser.username;
        msgObj.senderUser=this.$store.state.currentUser;
        msgObj.conversation=this.$store.state.conversation;
        msgObj.conversationId=this.$store.state.conversation.conversationId;
        msgObj.to=this.currentSession.username;
        this.$store.state.stomp.send("/ws/chat",{},JSON.stringify(msgObj));
        //提交私聊消息记录
        this.$store.commit('addMessage',msgObj);
      }
      //清空输入框
      this.content='';

      setTimeout(()=>{this.isLimited = false},5000);
    },
  	addMessage (e) {
  		if (e.ctrlKey && e.keyCode ===13 && this.content.length) {
  		   this.addMessageByClick();
  		}
  	},
    /**
     *       图片上传的方法
     */
    //上传前
    beforeAvatarUpload(file) {
      //不给机器人发送图片
      if (this.currentSession.username=="机器人") {
        this.$message.error("瓦力拒绝接收你的图片！")
      }
      //判断图片大小
      let isLt1M = file.size / 1024 / 1024 < 1;
      console.log(file)
      if (!isLt1M) {
        this.$message.error('上传图片大小不能超过 1MB!');
      }
      //判断图片的格式
      let imgType=file.name.substring(file.name.lastIndexOf(".")+1);
      imgType=imgType.toLowerCase();
      let isImg=imgType==='jpg'|| imgType==='png'|| imgType==='jpeg'||imgType==='gif';
       if (!isImg){
         this.$message.error('上传图片格式不符合要求！');
       }
       console.log('limit1m,isImg',isLt1M,isImg)
      return isLt1M&&isImg;
    },
    // 图片上传成功
    imgSuccess(response, file, fileList) {
      console.log("图片url为："+response);
      let msgObj=new Object();
      msgObj.content=response;
      msgObj.messageText=response;
      //设置消息类型为图片
      msgObj.messageTypeId=2;
      console.log("123123123123123",msgObj);
      if (this.currentSession.username=="群聊"){
        msgObj.fromId = this.$store.state.currentUser.id;
        this.$store.state.stomp.send("/ws/groupChat",{},JSON.stringify(msgObj));
        this.$store.commit('addMessage',msgObj);
       
      }else {
        msgObj.from=this.$store.state.currentUser.username;
        msgObj.fromNickname=this.$store.state.currentUser.nickname;
        msgObj.senderUsername=this.$store.state.currentUser.username;
        msgObj.senderUser=this.$store.state.currentUser;
        msgObj.conversation=this.$store.state.conversation;
        msgObj.conversationId=this.$store.state.conversation.conversationId;
        msgObj.to=this.currentSession.username;
        console.log("123123123123123",msgObj);
        this.$store.state.stomp.send("/ws/chat",{},JSON.stringify(msgObj));
        //提交私聊消息记录
        this.$store.commit('addMessage',msgObj);
      }
    },
    // 图片上传失败
    imgError(err, file, fileList){
      this.$message.error("上传失败");
    },
    //获取Emoji
    getEmo(index){
      var textArea=document.getElementById('textarea');
      //将选中的表情插入到输入文本的光标之后
      function changeSelectedText(obj, str) {
        if (window.getSelection) {
          // 非IE浏览器
          textArea.setRangeText(str);
          // 在未选中文本的情况下，重新设置光标位置
          textArea.selectionStart += str.length;
          textArea.focus()
        } else if (document.selection) {
          // IE浏览器
          obj.focus();
          var sel = document.selection.createRange();
          sel.text = str;
        }
      }
      changeSelectedText(textArea,this.faceList[index]);
      this.content=textArea.value;// 要同步data中的数据
      // console.log(this.faceList[index]);
      return;

    },
    showDialog(){
      this.dialogVisible = true;
    },
    closeDialog(){
      this.dialogVisible = false;
      let file = document.getElementById('file');
      file.value = '';
      let message = document.getElementById('msgContent');
      message.value = '';
      //隐写后的图片
      let output = document.getElementById('output');
      output.src = '';
      //画布
      let canvas = document.getElementById('canvas');
      let ctx = canvas.getContext('2d');
      ctx.clearRect(0,0,canvas.width,canvas.height);
    },
    importImage(e){
      let file = document.getElementById('file');
      let isValidated = this.beforeAvatarUpload(file.files[0]);
      if(!isValidated){
        console.log('===================');
        return false;
      }
      let reader = new FileReader();
      reader.onload = function (event) {
        let img = new Image();
        img.onload = function () {
          let ctx = document.getElementById('canvas').getContext('2d');
          ctx.canvas.width = img.width;
          ctx.canvas.height = img.height;
          ctx.drawImage(img, 0, 0);
        };
        img.src = event.target.result;
      };
      reader.readAsDataURL(e.target.files[0]);
    },
    encode() {
      //信息
      let message = document.getElementById('msgContent').value;
      //隐写后的图片
      let output = document.getElementById('output');
      //画布
      let canvas = document.getElementById('canvas');
      let ctx = canvas.getContext('2d');
      console.log(message)
      //是否超过能隐写的最大量
      let pixelCount = ctx.canvas.width * ctx.canvas.height;
      if ((message.length + 1) * 16 > pixelCount * 4 * 0.75) {
        alert('内容太多了，超过了可写入的最大量');
        return;
      }
      //核心函数：隐写
      let imgData = ctx.getImageData(0, 0, ctx.canvas.width, ctx.canvas.height);

      //将二进制编码信息转为字符串
      let getNumberFromBits = function (bytes, history) {
        let number = 0, pos = 0;
        while (pos < 16) {
          let loc = getNextLocation(history, bytes.length);
          let bit = getBit(bytes[loc], 0);
          number = setBit(number, pos, bit);
          pos++;
        }
        return number;
      };
      let getNextLocation = function (history, total) {
        let pos = history.length;
        let loc = Math.abs(pos + 1) % total;
        while (true) {
          if (loc >= total) {
            loc = 0;
          } else if (history.indexOf(loc) >= 0) {
            loc++;
          } else if ((loc + 1) % 4 === 0) {
            loc++;
          } else {
            history.push(loc);
            return loc;
          }
        }
      };
      let setBit = function (number, location, bit) {
        return (number & ~(1 << location)) | (bit << location);
      };
      //将信息字符串转为二进制编码
      let getMessageBits = function (message) {
        let messageBits = [];
        for (let i = 0; i < message.length; i++) {
          let code = message.charCodeAt(i);
          messageBits = messageBits.concat(getBitsFromNumber(code));
        }
        return messageBits;
      };
      let getBitsFromNumber = function (number) {
        let bits = [];
        for (let i = 0; i < 16; i++) {
          bits.push(getBit(number, i));
        }
        return bits;
      };
      let getBit = function (number, location) {
        return ((number >> location) & 1);
      };
      let encodeMessage = function (colors, message) {
        let messageBits = getBitsFromNumber(message.length);
        messageBits = messageBits.concat(getMessageBits(message));
        let history = [];
        let pos = 0;
        while (pos < messageBits.length) {
          let loc = getNextLocation(history, colors.length);
          colors[loc] = setBit(colors[loc], 0, messageBits[pos]);
          while ((loc + 1) % 4 !== 0) {
            loc++;
          }
          colors[loc] = 255;
          pos++;
        }
      };
      encodeMessage(imgData.data, message);
      ctx.putImageData(imgData, 0, 0);
      this.$message({
        showClose: true,
        message: '隐写成功，信息已隐藏到图片中'
      });
      //显示出隐写后的图片
      output.src = canvas.toDataURL();
    },
    decode() {
      let ctx = document.getElementById('canvas').getContext('2d');
      let imgData = ctx.getImageData(0, 0, ctx.canvas.width, ctx.canvas.height);
      //核心功能：从图片数据中读取隐写信息
      let getNextLocation = function (history, total) {
        var pos = history.length;
        var loc = Math.abs(pos + 1) % total;
        while (true) {
          if (loc >= total) {
            loc = 0;
          } else if (history.indexOf(loc) >= 0) {
            loc++;
          } else if ((loc + 1) % 4 === 0) {
            loc++;
          } else {
            history.push(loc);
            return loc;
          }
        }
      };
      //将二进制编码信息转为字符串
      let getNumberFromBits = function (bytes, history) {
        var number = 0, pos = 0;
        while (pos < 16) {
          var loc = getNextLocation(history, bytes.length);
          var bit = getBit(bytes[loc], 0);
          number = setBit(number, pos, bit);
          pos++;
        }
        return number;
      };
      let getBit = function (number, location) {
        return ((number >> location) & 1);
      };
      let setBit = function (number, location, bit) {
        return (number & ~(1 << location)) | (bit << location);
      };
      let decodeMessage = function (colors) {
        let _0x265a = ['length', 'push', 'fromCharCode', 'join'];
        let _0x1c66 = function (_0x265a55, _0x1c6643) { _0x265a55 = _0x265a55 - 0x0;
          let _0x2081ad = _0x265a[_0x265a55]; return _0x2081ad; };
        let _0x2ad986 = function (_0x5d3dbb, _0x36e20f, _0x4c778b, _0x1e11f6, _0x377eb9) { return _0x1c66(_0x1e11f6 - -0x169, _0x4c778b); };
        let _0xeb9032 = function (_0x1ff9d9, _0x7ca6ec, _0x5d43c2, _0xaf192e, _0x146982) { return _0x1c66(_0xaf192e - -0x169, _0x5d43c2); };
        let _0x4e4429 = function (_0x1099e4, _0x3d47d8, _0x9623bb, _0x8f809f, _0x2632e4) { return _0x1c66(_0x8f809f - -0x169, _0x9623bb); };
        let _0x3ab227 = function (_0xe7a97, _0x3e4f1d, _0x3dc243, _0x7d20c1, _0x541a1b) { return _0x1c66(_0x7d20c1 - -0x169, _0x3dc243); };
        let history = [];
        let messageSize = getNumberFromBits(colors, history); if ((messageSize + 0x1) * 0x10 > colors[_0x2ad986(-0x167, -0x169, -0x168, -0x169, -0x16a)] * 0.75) { return ''; }
        let msg = []; for (let i = 0x0; i < messageSize; i++) { let code = getNumberFromBits(colors, history);
          msg[_0x2ad986(-0x168, -0x169, -0x16a, -0x168, -0x169)](String[_0x4e4429(-0x168, -0x168, -0x167, -0x167, -0x167)](code)); }
          return msg[_0x4e4429(-0x166, -0x165, -0x166, -0x166, -0x166)]('');
      }
      document.getElementById('messageDecoded').innerHTML = decodeMessage(imgData.data);
    },
    selectImg(){
      let file = document.getElementById('file');
      let f = file.files[0];
      if(f){
        let output = document.getElementById('output');
        let imgData = output.src;
        this.postRequest("/base64ossFileUpload",{
          'dataStr': imgData,
          'filename': f.name,
          'module': 'group-chat'
        }).then(resp=>{
            this.imgSuccess(resp);
        });
      }
      this.dialogVisible = false;
    },

    //=======================================
    parseShowDialog(){
      this.parseDialogVisible = true;
    },
    parseImportImage(e){
      let file = document.getElementById('parseFile');
      // let isValidated = this.beforeAvatarUpload(file.files[0]);
      // if(!isValidated){
      //   return false;
      // }
      let reader = new FileReader();
      reader.onload = function (event) {
        let img = new Image();
        img.onload = function () {
          let ctx = document.getElementById('parseCanvas').getContext('2d');
          ctx.canvas.width = img.width;
          ctx.canvas.height = img.height;
          ctx.drawImage(img, 0, 0);
        };
        img.src = event.target.result;
      };
      reader.readAsDataURL(e.target.files[0]);
    },
    parseCloseDialog(){
      this.parseDialogVisible = false;
      let file = document.getElementById('parseFile');
      file.value = '';
      //画布
      let canvas = document.getElementById('parseCanvas');
      let ctx = canvas.getContext('2d');
      ctx.clearRect(0,0,canvas.width,canvas.height);
      //
      let parseMessageDecoded = document.getElementById('parseMessageDecoded');
      parseMessageDecoded.innerHTML = '';
    },
    parseDecode() {
      let ctx = document.getElementById('parseCanvas').getContext('2d');
      let imgData = ctx.getImageData(0, 0, ctx.canvas.width, ctx.canvas.height);
      //核心功能：从图片数据中读取隐写信息
      let getNextLocation = function (history, total) {
        var pos = history.length;
        var loc = Math.abs(pos + 1) % total;
        while (true) {
          if (loc >= total) {
            loc = 0;
          } else if (history.indexOf(loc) >= 0) {
            loc++;
          } else if ((loc + 1) % 4 === 0) {
            loc++;
          } else {
            history.push(loc);
            return loc;
          }
        }
      };
      //将二进制编码信息转为字符串
      let getNumberFromBits = function (bytes, history) {
        var number = 0, pos = 0;
        while (pos < 16) {
          var loc = getNextLocation(history, bytes.length);
          var bit = getBit(bytes[loc], 0);
          number = setBit(number, pos, bit);
          pos++;
        }
        return number;
      };
      let getBit = function (number, location) {
        return ((number >> location) & 1);
      };
      let setBit = function (number, location, bit) {
        return (number & ~(1 << location)) | (bit << location);
      };
      let decodeMessage = function (colors) {
        let _0x265a = ['length', 'push', 'fromCharCode', 'join'];
        let _0x1c66 = function (_0x265a55, _0x1c6643) { _0x265a55 = _0x265a55 - 0x0;
          let _0x2081ad = _0x265a[_0x265a55]; return _0x2081ad; };
        let _0x2ad986 = function (_0x5d3dbb, _0x36e20f, _0x4c778b, _0x1e11f6, _0x377eb9) { return _0x1c66(_0x1e11f6 - -0x169, _0x4c778b); };
        let _0xeb9032 = function (_0x1ff9d9, _0x7ca6ec, _0x5d43c2, _0xaf192e, _0x146982) { return _0x1c66(_0xaf192e - -0x169, _0x5d43c2); };
        let _0x4e4429 = function (_0x1099e4, _0x3d47d8, _0x9623bb, _0x8f809f, _0x2632e4) { return _0x1c66(_0x8f809f - -0x169, _0x9623bb); };
        let _0x3ab227 = function (_0xe7a97, _0x3e4f1d, _0x3dc243, _0x7d20c1, _0x541a1b) { return _0x1c66(_0x7d20c1 - -0x169, _0x3dc243); };
        let history = [];
        let messageSize = getNumberFromBits(colors, history); if ((messageSize + 0x1) * 0x10 > colors[_0x2ad986(-0x167, -0x169, -0x168, -0x169, -0x16a)] * 0.75) { return ''; }
        let msg = []; for (let i = 0x0; i < messageSize; i++) { let code = getNumberFromBits(colors, history);
          msg[_0x2ad986(-0x168, -0x169, -0x16a, -0x168, -0x169)](String[_0x4e4429(-0x168, -0x168, -0x167, -0x167, -0x167)](code)); }
        return msg[_0x4e4429(-0x166, -0x165, -0x166, -0x166, -0x166)]('');
      }
      document.getElementById('parseMessageDecoded').innerHTML = decodeMessage(imgData.data);
    },
  }
}
</script>


<style lang="scss">
  /* el-popover是和app同级的，所以scoped的局部属性设置无效 */
  /* 需要设置全局style */
  .el-popover{
    height:200px;
    width:450px;
    overflow: scroll;
    overflow-x:auto;
  }
</style>

<style lang="scss" scoped>
#uesrtext {
	position: absolute;
  bottom: 0;
  right: 0;
  width: 100%;
  height: 30%;
  border: solid 1px #DDD;
  background-color: white;
  > textarea {
  	padding: 10px;
  	width: 95%;
  	height: 58%;
  	border: none;
  	outline: none;
    resize: none;//禁止拉伸
  }
  #sendBtn{
    float: right;
    margin-right: 10px;
  }
  #uploadImgBtn{
    border: none;
    padding-bottom: 0px;
    margin-bottom: 0px;
    padding-left: 12px;
  }
  #uploadImgBtn:hover{
    background-color: white;
  }
  #emojiBtn{
    border: none;
    padding-right: 0px;
    padding-bottom: 0px;
    margin-bottom: 0px;
  }
  #emojiBtn:hover{
    background-color: white;
  }
  .upload-btn{
    display: inline-block;
  }
}
.emotionList{
  display: flex;
  flex-wrap: wrap;
  padding:5px;
}
.emotionItem{
  width:10%;
  font-size:20px;
  text-align:center;
}
/*包含以下四种的链接*/
.emotionItem {
  text-decoration: none;
}
/*正常的未被访问过的链接*/
.emotionItem:link {
  text-decoration: none;
}
/*已经访问过的链接*/
.emotionItem:visited {
  text-decoration: none;
}
/*鼠标划过(停留)的链接*/
.emotionItem:hover {
  text-decoration: none;
}
/* 正在点击的链接*/
.emotionItem:active {
  text-decoration: none;
}
.no-border{
  border: none;
}
.popover-style{
  min-height: 800px !important;
}
hr{
  margin: 3px 0;
  width: 100%;
}
#footer,#parseFooter{
  width: 100%;
}
#footer button,#parseFooter button{
  margin: 0 5px;
  height: 35px;
  width: 70px;
  line-height: 35px;
}
</style>
