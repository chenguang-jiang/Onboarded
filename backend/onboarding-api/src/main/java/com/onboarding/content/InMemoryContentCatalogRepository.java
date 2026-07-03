package com.onboarding.content;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Repository;

import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

@Repository
@Profile("standalone")
public class InMemoryContentCatalogRepository implements ContentCatalogRepository {

    private final List<Chapter> chapters = List.of(
            new Chapter(1, "计算机系统基础", "CPU、存储、总线与可靠性基础。", 1),
            new Chapter(2, "软件架构设计", "架构风格、质量属性与架构评估。", 2),
            new Chapter(3, "系统分析与建模", "需求、UML、业务流程与领域建模。", 3),
            new Chapter(4, "数据库与数据架构", "关系模型、事务、索引与数据治理。", 4),
            new Chapter(5, "安全与项目管理", "安全体系、风险、进度与质量管理。", 5)
    );

    private final List<KnowledgePoint> knowledgePoints = List.of(
            kp(1, 1, "流水线吞吐率", "流水线稳定后吞吐率由最慢流水段决定。", "MEDIUM", "计算机组成", "性能"),
            kp(2, 1, "Cache 命中率", "Cache 性能由命中率、命中时间和失效代价共同决定。", "MEDIUM", "存储", "性能"),
            kp(3, 1, "系统可靠性", "串联系统可靠性相乘，并联系统可靠性按失效概率相乘后取补。", "HARD", "可靠性", "计算"),
            kp(4, 2, "分层架构", "分层架构强调职责隔离，上层依赖下层提供的抽象服务。", "EASY", "架构风格", "分层"),
            kp(5, 2, "微服务边界", "微服务边界通常围绕业务能力和数据自治划分。", "MEDIUM", "微服务", "领域"),
            kp(6, 2, "ATAM 评估", "ATAM 通过场景分析架构决策对质量属性的影响。", "HARD", "质量属性", "评估"),
            kp(7, 3, "用例建模", "用例描述参与者与系统为了目标完成的一组交互。", "EASY", "UML", "需求"),
            kp(8, 3, "领域模型", "领域模型关注核心概念、属性与关系，不急于落到数据库表。", "MEDIUM", "DDD", "建模"),
            kp(9, 3, "活动图", "活动图适合表达业务流程、分支、并行和对象流。", "EASY", "UML", "流程"),
            kp(10, 4, "事务 ACID", "ACID 分别对应原子性、一致性、隔离性和持久性。", "EASY", "事务", "数据库"),
            kp(11, 4, "索引选择性", "高选择性字段更适合建立索引以减少扫描范围。", "MEDIUM", "索引", "性能"),
            kp(12, 4, "数据仓库分层", "ODS、DWD、DWS、ADS 用于组织从原始到应用的数据链路。", "MEDIUM", "数仓", "数据架构"),
            kp(13, 5, "身份认证与授权", "认证确认你是谁，授权决定你能访问什么。", "EASY", "安全", "权限"),
            kp(14, 5, "风险应对策略", "风险应对包括规避、转移、减轻和接受。", "MEDIUM", "项目管理", "风险"),
            kp(15, 5, "关键路径", "关键路径决定项目最短工期，路径上的活动延期会影响总工期。", "MEDIUM", "项目管理", "进度")
    );

    private final List<PracticeQuestion> questions = List.of(
            question(1, 1, "流水线处理器稳定运行后的吞吐率主要受哪个因素影响？", "最慢流水段时间"),
            question(2, 2, "评价 Cache 平均访问时间时，通常不需要考虑的是？", "显示器刷新率"),
            question(3, 3, "两个可靠度分别为 R1、R2 的串联系统，总可靠度为？", "R1 * R2"),
            question(4, 4, "分层架构最核心的设计收益是？", "职责隔离与依赖有序"),
            question(5, 5, "划分微服务边界时更应优先考虑？", "业务能力和数据自治"),
            question(6, 6, "ATAM 主要用于评估架构对什么的影响？", "质量属性"),
            question(7, 7, "用例建模重点描述的是？", "参与者与系统围绕目标的交互"),
            question(8, 8, "领域模型最应该优先表达的是？", "业务概念及其关系"),
            question(9, 9, "活动图最适合表达哪类内容？", "业务流程与并发分支"),
            question(10, 10, "ACID 中 I 表示？", "隔离性"),
            question(11, 11, "下列字段中通常更适合建立高效索引的是？", "高选择性字段"),
            question(12, 12, "数仓分层中 ADS 通常面向？", "应用和报表服务"),
            question(13, 13, "认证和授权的区别是？", "认证识别身份，授权控制权限"),
            question(14, 14, "购买保险属于哪种风险应对方式？", "转移"),
            question(15, 15, "关键路径上的活动延期通常会导致？", "项目总工期延长")
    );

    private final Map<Long, PracticeQuestion> questionsByKnowledgePointId =
            questions.stream().collect(Collectors.toUnmodifiableMap(PracticeQuestion::knowledgePointId, Function.identity()));

    private final Map<Long, PracticeQuestion> questionsById =
            questions.stream().collect(Collectors.toUnmodifiableMap(PracticeQuestion::id, Function.identity()));

    private final Map<Long, KnowledgePoint> knowledgePointsById =
            knowledgePoints.stream().collect(Collectors.toUnmodifiableMap(KnowledgePoint::id, Function.identity()));

    private final Map<Long, Chapter> chaptersById =
            chapters.stream().collect(Collectors.toUnmodifiableMap(Chapter::id, Function.identity()));

    @Override
    public List<Chapter> listChapters() {
        return chapters.stream().sorted(Comparator.comparingInt(Chapter::sortOrder)).toList();
    }

    @Override
    public List<KnowledgePoint> listKnowledgePoints() {
        return knowledgePoints;
    }

    @Override
    public Optional<Chapter> getChapter(long chapterId) {
        return Optional.ofNullable(chaptersById.get(chapterId));
    }

    @Override
    public Optional<KnowledgePoint> getKnowledgePointById(long knowledgePointId) {
        return Optional.ofNullable(knowledgePointsById.get(knowledgePointId));
    }

    @Override
    public Optional<PracticeQuestion> getQuestionByKnowledgePointId(long knowledgePointId) {
        return Optional.ofNullable(questionsByKnowledgePointId.get(knowledgePointId));
    }

    @Override
    public Optional<PracticeQuestion> getQuestionById(long questionId) {
        return Optional.ofNullable(questionsById.get(questionId));
    }

    private static KnowledgePoint kp(
            long id,
            long chapterId,
            String title,
            String summary,
            String difficulty,
            String... tags
    ) {
        return new KnowledgePoint(id, chapterId, title, summary, difficulty, List.of(tags));
    }

    private static PracticeQuestion question(long id, long knowledgePointId, String stem, String answerText) {
        return new PracticeQuestion(
                id,
                knowledgePointId,
                stem,
                List.of(
                        new QuestionOption("A", answerText),
                        new QuestionOption("B", "仅由开发语言决定"),
                        new QuestionOption("C", "只与界面原型有关"),
                        new QuestionOption("D", "可以完全忽略业务场景")
                ),
                "A",
                "本题考查该知识点的核心定义或常见计算规则。"
        );
    }
}
