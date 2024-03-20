<template>
  <div>
    <div class="top-controls">
      <span>Private Chat Management</span>
      <el-input placeholder="Enter user nickname to search"
                prefix-icon="el-icon-search"
                v-model="nameKeyword"
                class="search-input"></el-input>
      <el-date-picker
          v-model="dateScope"
          type="datetimerange"
          range-separator="to"
          start-placeholder="Start date"
          end-placeholder="End date"
          :editable="false"
          :unlink-panels="true"
          value-format="yyyy-MM-dd HH:mm:ss"
          class="date-picker"></el-date-picker>
      <el-select v-model="selectedType" placeholder="Message Type">
        <el-option label="All" :value="-1"></el-option>
        <el-option label="Text" :value="1"></el-option>
        <el-option label="Image" :value="2"></el-option>
        <el-option label="File" :value="3"></el-option>
      </el-select>
      <el-button icon="el-icon-search" type="primary" @click="fetchData">Search</el-button>
      <el-button icon="el-icon-refresh" type="primary" @click="refreshData">Refresh</el-button>
      <el-button icon="el-icon-download" type="success" @click="exportData">Export</el-button>
    </div>
    <div style="margin-top: 15px">
      <el-table
          :data="messTableData"
          v-loading="loading"
          border
          stripe
          @selection-change="handleSelectionChange"
          style="width: 100%">
        <el-table-column
            type="selection"
            width="55">
        </el-table-column>
        <el-table-column
            prop="id"
            label="Message ID"
            width="80">
        </el-table-column>
        <el-table-column
            prop="fromId"
            label="Sender ID"
            width="100">
        </el-table-column>
        <el-table-column
            prop="fromName"
            label="Sender Nickname"
            width="150">
        </el-table-column>
        <el-table-column
            prop="createTime"
            label="Send Time"
            width="180">
        </el-table-column>
        <el-table-column
            label="Content"
            width="750">
          <template slot-scope="scope">
            <div v-if="scope.row.messageTypeId==1" v-html="scope.row.content"></div>
            <div v-else>
              <el-image :src="scope.row.content"
                        :preview-src-list="[scope.row.content]"
                        style="width: 50px;height: 50px">
                <div slot="error" class="image-slot">
                  <i class="el-icon-picture-outline"></i>
                </div>
              </el-image>
            </div>
          </template>
        </el-table-column>
        <el-table-column label="Action">
          <template slot-scope="scope">
            <el-button
                size="mini"
                type="danger"
                @click="handleDelete(scope.row)">Delete</el-button>
          </template>
        </el-table-column>
      </el-table>
    </div>

    <!-- ... -->
  </div>
</template>

<script>
export default {
  data() {
    return {
      messages: [],
      nameKeyword: '',
      dateScope: null,
      selectedType: -1,
      loading: false,
      page:1,//起始页数
      size:10,//单页显示数据数
      total:-1,//表格数据总数
      // other necessary data properties
    };
  },
  methods: {
    initMessTableData(){
      this.loading=true;
      let url="/groupMsgContent/page?page="+this.page+"&size="+this.size;
      this.getRequest(url).then(resp=>{
        this.messTableData=resp.data;
        this.total=resp.total;
        setTimeout(()=>{
          this.loading=false;
        },1000)
      })
    },
    fetchData() {
      // Implement method to fetch private chat records
    },
    refreshData() {
      // Implement method to refresh the data on the page
    },
    exportData() {
      // Implement method to export data to an Excel file
    },
    // other necessary methods
  },
  mounted() {
    this.fetchData();
    this.initMessTableData()
  },

};
</script>

<style scoped>
.top-controls {

}
.search-input, .date-picker {

}

</style>



