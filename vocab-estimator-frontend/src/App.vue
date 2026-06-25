<template>
  <div id="app">
    <el-container style="height: 100vh;">
      <el-aside width="220px" style="background-color: #304156;">
        <el-menu
          :default-active="route.path"
          router
          background-color="#304156"
          text-color="#bfcbd9"
          active-text-color="#409EFF"
          style="height: 100%; border-right: none;"
        >
          <div style="padding: 20px; text-align: center; color: #fff; font-size: 16px; font-weight: bold;">
            <el-icon style="vertical-align: middle; margin-right: 5px;"><Coin /></el-icon>
            词汇量估算工具
          </div>
          <el-menu-item index="/">
            <el-icon><HomeFilled /></el-icon>
            <span>首页</span>
          </el-menu-item>
          <el-menu-item index="/online-test">
            <el-icon><Edit /></el-icon>
            <span>在线测试</span>
          </el-menu-item>
          <el-menu-item index="/batch-manage">
            <el-icon><Upload /></el-icon>
            <span>批处理</span>
          </el-menu-item>
          <el-menu-item index="/corpus-analysis">
            <el-icon><DataAnalysis /></el-icon>
            <span>语料分析</span>
          </el-menu-item>
          <el-menu-item index="/validation">
            <el-icon><Check /></el-icon>
            <span>算法验证</span>
          </el-menu-item>
          <el-menu-item index="/stats">
            <el-icon><TrendCharts /></el-icon>
            <span>统计报表</span>
          </el-menu-item>
          <el-menu-item index="/word-library">
            <el-icon><Notebook /></el-icon>
            <span>词汇库</span>
          </el-menu-item>
        </el-menu>
      </el-aside>

      <el-container>
        <el-header style="background: #fff; border-bottom: 1px solid #e6e6e6; display: flex; align-items: center; justify-content: space-between; padding: 0 20px;">
          <span style="font-size: 18px; font-weight: 500; color: #303133;">
            英语词汇量估算工具
          </span>
          <div>
            <el-tag type="info" effect="plain">v1.0.0</el-tag>
          </div>
        </el-header>

        <el-main style="background-color: #f0f2f5; padding: 20px;">
          <router-view />
        </el-main>
      </el-container>
    </el-container>
  </div>
</template>

<script setup>
import { useRoute } from 'vue-router'
const route = useRoute()
</script>

<style>
body {
  margin: 0;
  padding: 0;
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, 'Helvetica Neue', Arial, sans-serif;
}
</style>