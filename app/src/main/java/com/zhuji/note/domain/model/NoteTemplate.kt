package com.zhuji.note.domain.model

data class NoteTemplate(
    val id: String,
    val name: String,
    val icon: String,
    val titleHint: String,
    val contentTemplate: String,
)

object BuiltInTemplates {
    val all = listOf(
        NoteTemplate("blank", "空白笔记", "📝", "标题", ""),
        NoteTemplate("diary", "日记", "📖", "今天的心情", "## {{date}}\n\n### 今日亮点\n- \n\n### 感恩\n- \n\n### 明日计划\n- \n"),
        NoteTemplate("meeting", "会议纪要", "🤝", "会议主题", "## 会议信息\n- **日期**: {{date}}\n- **参会人**: \n- **地点**: \n\n## 议题\n1. \n\n## 决议\n- \n\n## 待办\n- [ ] \n"),
        NoteTemplate("reading", "读书笔记", "📚", "书名", "## 基本信息\n- **作者**: \n- **出版年**: \n- **评分**: ⭐⭐⭐⭐⭐\n\n## 核心观点\n\n## 金句摘录\n> \n\n## 我的思考\n\n## 行动清单\n- [ ] \n"),
        NoteTemplate("todo", "待办清单", "✅", "清单名称", "## 优先级高\n- [ ] \n\n## 优先级中\n- [ ] \n\n## 优先级低\n- [ ] \n"),
        NoteTemplate("idea", "灵感速记", "💡", "灵感标题", "## 核心想法\n\n## 为什么重要\n\n## 下一步\n- \n\n## 相关链接\n- [[]] \n"),
        NoteTemplate("weekly", "周报", "📊", "第 N 周周报", "## 本周完成\n- \n\n## 本周数据\n| 指标 | 目标 | 实际 |\n|------|------|------|\n|      |      |      |\n\n## 下周计划\n- \n\n## 风险/阻塞\n- \n"),
        NoteTemplate("cornell", "康奈尔笔记", "🎓", "课程/主题", "## 关键词/问题\n\n---\n\n## 笔记区\n\n---\n\n## 总结\n\n"),
    )
}
