<template>
  <div id="card">
  	<header>

  	</header>
  	<footer>

  	</footer>

  </div>
</template>

<script>
export default {
  name: 'card',
  data () {
    return {
      user:JSON.parse(window.sessionStorage.getItem('user'))
    }
  }
}
</script>

<style lang="scss" scoped>
#card {
	padding: 12px;
	margin-bottom: 5px;
  .avatar{
  	width: 40px;
  	height: 40px;
  	vertical-align: middle;
  }
  .name {
  	display: inline-block;
  	padding: 10px;
  	margin-bottom: 15px;
  	font-size: 16px;
  }
  .search {
  	background-color: #DBD9D8;
		width: 150px;
  	height: 30px;
  	line-height: 30px;
  	padding: 0 10px;
  	border: 0;
  	border-radius: 4px;
  	outline: none;
    color: #FFF;
  }
	.searchBtn{
		background-color: #DBD9D8;
		border: 0;
		margin-left: 3px;
	}
}
</style>
<style>

	#card .el-input__inner {
		background-color: #DBD9D8;
		outline: none;
	}
</style>
