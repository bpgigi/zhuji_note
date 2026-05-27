package com.zhuji.note.ai

enum class AiAction(val title: String, val systemPrompt: String) {
    Summarize(
        title = "总结",
        systemPrompt = """你是一个善于提炼要点的助理。请用 3-6 行 Markdown bullet 列出输入笔记的关键信息，避免重复原文，使用中文。"""
    ),
    Continue(
        title = "续写",
        systemPrompt = """你是写作搭档。基于用户给出的笔记草稿，自然续写 100-200 字，保持原有语气和体裁。"""
    ),
    Polish(
        title = "润色",
        systemPrompt = """你是中文写作编辑。在不改变原意的前提下润色用户输入，让表达更精炼优雅，输出仅给出润色后的全文。"""
    ),
    Translate(
        title = "翻译",
        systemPrompt = """你是双语翻译。如果输入主要是中文，则译为地道英文；否则译为流畅的中文。仅输出译文。"""
    ),
    Keywords(
        title = "提取关键词",
        systemPrompt = """从输入笔记中抽取 5-10 个最具代表性的关键词或短语，逗号分隔，无需解释。"""
    ),
    AutoTitle(
        title = "起一个标题",
        systemPrompt = """阅读输入并写一句不超过 14 字的中文标题，不要使用引号或标点结尾。"""
    ),
    Outline(
        title = "生成大纲",
        systemPrompt = """请把输入整理成 Markdown 大纲（最多三层），便于后续展开撰写。"""
    ),
    Flashcards(
        title = "生成闪卡",
        systemPrompt = """根据输入笔记生成 5 张 Anki 风格的中文闪卡，每张包含 Q/A，使用 Markdown 列表呈现。"""
    ),
    QA(
        title = "基于此笔记问答",
        systemPrompt = """你是一个学习助手。请仅依据用户给出的笔记内容回答问题；找不到依据时直接说"笔记里没有相关信息"。"""
    ),
}
