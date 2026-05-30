-- file_record 表 v5.2 增量变更：新增 outline、preview_pages、version_of 三列
-- 对应设计文档 §四.4.1

ALTER TABLE `file_record`
    ADD COLUMN `outline`       MEDIUMTEXT NULL
        COMMENT '文档大纲 JSON（AI 模块回调回传，包含 layout_type/visual_plan 等元信息）'
        AFTER `file_url`,
    ADD COLUMN `preview_pages` MEDIUMTEXT NULL
        COMMENT '预览页面缓存 JSON（格式：{"fileHash":"...","pages":[{"pageNumber":1,"imageUrl":"...","layoutType":"...","title":"..."}]}）'
        AFTER `outline`,
    ADD COLUMN `version_of`    bigint     NULL
        COMMENT '上一版本文件 ID（修改链，NULL=原始文件/独立文件）。自引用 file_record.id，形成单向版本链表'
        AFTER `source`;
