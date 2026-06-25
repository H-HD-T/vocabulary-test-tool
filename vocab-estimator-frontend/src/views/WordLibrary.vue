<template>
  <div class="word-library">
    <el-card>
      <template #header>
        <div style="display: flex; justify-content: space-between; align-items: center;">
          <span>词汇库管理</span>
          <div>
            <el-button type="primary" size="small" @click="showAddDialog">添加单词</el-button>
          </div>
        </div>
      </template>
      <el-form :model="searchForm" inline>
        <el-form-item label="关键词">
          <el-input v-model="searchForm.keyword" placeholder="单词/释义" clearable />
        </el-form-item>
        <el-form-item label="难度">
          <el-select v-model="searchForm.difficulty" clearable placeholder="全部">
            <el-option label="K (小学)" value="K" />
            <el-option label="P (初中)" value="P" />
            <el-option label="F (高中)" value="F" />
            <el-option label="C (大学)" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item>
          <el-button type="primary" @click="search">搜索</el-button>
          <el-button @click="resetSearch">重置</el-button>
        </el-form-item>
      </el-form>
      <el-table :data="wordList" border stripe v-loading="loading">
        <el-table-column prop="id" label="ID" width="60" />
        <el-table-column prop="word" label="单词" width="140" />
        <el-table-column prop="difficulty" label="等级" width="80">
          <template #default="{ row }">
            <el-tag :type="levelTagType(row.difficulty)" size="small">{{ row.difficulty }}</el-tag>
          </template>
        </el-table-column>
        <el-table-column prop="frequency" label="词频" width="100">
          <template #default="{ row }">
            <el-progress :percentage="Math.round((row.frequency || 0) * 100)" :width="60" type="circle" :stroke-width="6" />
          </template>
        </el-table-column>
        <el-table-column prop="definition" label="释义" min-width="200" show-overflow-tooltip />
        <el-table-column prop="cetLabel" label="四六级标签" width="100">
          <template #default="{ row }">
            <el-tag v-if="row.cetLabel !== 'NONE'" size="small">{{ row.cetLabel }}</el-tag>
            <span v-else>-</span>
          </template>
        </el-table-column>
        <el-table-column label="操作" width="180" fixed="right">
          <template #default="{ row }">
            <el-button size="small" @click="editWord(row)">编辑</el-button>
            <el-button size="small" type="danger" @click="deleteWordItem(row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>
      <div style="margin-top: 15px; display: flex; justify-content: flex-end;">
        <el-pagination
          v-model:current-page="pageParams.page"
          v-model:page-size="pageParams.size"
          :total="total"
          layout="prev, pager, next, total"
          @change="loadWords"
        />
      </div>
    </el-card>
    <el-dialog v-model="dialogVisible" :title="isEdit ? '编辑单词' : '添加单词'" width="500px">
      <el-form :model="wordForm" :rules="rules" ref="formRef" label-width="100px">
        <el-form-item label="单词" prop="word">
          <el-input v-model="wordForm.word" />
        </el-form-item>
        <el-form-item label="难度" prop="difficulty">
          <el-select v-model="wordForm.difficulty">
            <el-option label="K (小学)" value="K" />
            <el-option label="P (初中)" value="P" />
            <el-option label="F (高中)" value="F" />
            <el-option label="C (大学)" value="C" />
          </el-select>
        </el-form-item>
        <el-form-item label="词频" prop="frequency">
          <el-slider v-model="wordForm.frequency" :min="0" :max="1" :step="0.01" style="width: 300px;" />
        </el-form-item>
        <el-form-item label="释义" prop="definition">
          <el-input v-model="wordForm.definition" type="textarea" :rows="2" />
        </el-form-item>
        <el-form-item label="四六级标签">
          <el-select v-model="wordForm.cetLabel">
            <el-option label="无" value="NONE" />
            <el-option label="四级" value="CET4" />
            <el-option label="六级" value="CET6" />
            <el-option label="都有" value="BOTH" />
          </el-select>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="dialogVisible = false">取消</el-button>
        <el-button type="primary" @click="saveWord">保存</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { getWordPage, addWord, updateWord, deleteWord } from '@/api/wordApi'

const wordList = ref([])
const loading = ref(false)
const total = ref(0)
const dialogVisible = ref(false)
const isEdit = ref(false)
const formRef = ref(null)

const searchForm = reactive({
  keyword: '',
  difficulty: ''
})

const pageParams = reactive({
  page: 1,
  size: 20
})

const wordForm = reactive({
  id: null,
  word: '',
  difficulty: 'K',
  frequency: 0.5,
  definition: '',
  cetLabel: 'NONE'
})

const rules = {
  word: [{ required: true, message: '请输入单词', trigger: 'blur' }],
  difficulty: [{ required: true, message: '请选择难度等级', trigger: 'change' }]
}

function levelTagType(level) {
  const map = { K: 'info', P: 'primary', F: 'warning', C: 'danger' }
  return map[level] || 'info'
}

async function loadWords() {
  loading.value = true
  try {
    const params = {
      page: pageParams.page,
      size: pageParams.size,
      ...(searchForm.keyword && { keyword: searchForm.keyword }),
      ...(searchForm.difficulty && { difficulty: searchForm.difficulty })
    }
    const res = await getWordPage(params)
    if (res.code === 200) {
      wordList.value = res.data.records
      total.value = res.data.total
    }
  } catch (e) {
    console.log('加载词汇表失败')
  } finally {
    loading.value = false
  }
}

function search() {
  pageParams.page = 1
  loadWords()
}

function resetSearch() {
  searchForm.keyword = ''
  searchForm.difficulty = ''
  pageParams.page = 1
  loadWords()
}

function showAddDialog() {
  isEdit.value = false
  Object.assign(wordForm, { id: null, word: '', difficulty: 'K', frequency: 0.5, definition: '', cetLabel: 'NONE' })
  dialogVisible.value = true
}

function editWord(row) {
  isEdit.value = true
  Object.assign(wordForm, row)
  dialogVisible.value = true
}

async function saveWord() {
  try {
    if (isEdit.value) {
      await updateWord(wordForm)
      ElMessage.success('单词已更新')
    } else {
      await addWord(wordForm)
      ElMessage.success('单词已添加')
    }
    dialogVisible.value = false
    await loadWords()
  } catch (e) {
    ElMessage.error('保存失败')
  }
}

function deleteWordItem(row) {
  ElMessageBox.confirm('删除单词 "' + row.word + '"?', '确认', {
    type: 'warning'
  }).then(async () => {
    await deleteWord(row.id)
    ElMessage.success('单词已删除')
    await loadWords()
  }).catch(() => {})
}

onMounted(() => {
  loadWords()
})
</script>