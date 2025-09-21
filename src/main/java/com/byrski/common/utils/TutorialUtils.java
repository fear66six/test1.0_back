package com.byrski.common.utils;

import com.byrski.domain.entity.dto.Tutorial;
import com.byrski.domain.entity.dto.TutorialEdge;
import com.byrski.infrastructure.mapper.impl.TutorialEdgeMapperService;
import com.byrski.infrastructure.mapper.impl.TutorialMapperService;
import jakarta.annotation.PostConstruct;
import jakarta.annotation.Resource;
import lombok.Data;
import lombok.Getter;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

@Component
@Data
public class TutorialUtils {

    @Resource
    private TutorialMapperService tutorialMapperService;
    @Resource
    private TutorialEdgeMapperService tutorialEdgeMapperService;

    // 获取 DAG 图
    // 全局静态变量：DAG 图
    @Getter
    private static Graph<Tutorial, DefaultEdge> DAG = new DefaultDirectedGraph<>(DefaultEdge.class);

    // 静态服务实例
    private static TutorialMapperService staticTutorialMapperService;
    private static TutorialEdgeMapperService staticTutorialEdgeMapperService;

    // 锁用于线程安全的重载
    private static final ReentrantLock LOCK = new ReentrantLock();

    @PostConstruct
    public void init() {
        staticTutorialMapperService = this.tutorialMapperService;
        staticTutorialEdgeMapperService = this.tutorialEdgeMapperService;
        initializeDAG();
    }

    private static void initializeDAG() {
        LOCK.lock();
        try {
            DAG = new DefaultDirectedGraph<>(DefaultEdge.class);

            // 从数据库中读取节点和边
            List<Tutorial> tutorialList = staticTutorialMapperService.list();
            List<TutorialEdge> tutorialEdges = staticTutorialEdgeMapperService.list();

            // 将所有 Tutorial 添加为图的节点
            for (Tutorial tutorial : tutorialList) {
                DAG.addVertex(tutorial);  // 将 Tutorial 对象本身作为节点
            }

            // 添加边
            for (TutorialEdge edge : tutorialEdges) {
                Long startVertexId = edge.getStartVertex();
                Long endVertexId = edge.getEndVertex();

                // 查找对应的 Tutorial 对象
                Tutorial startVertex = findTutorialById(startVertexId, tutorialList);
                Tutorial endVertex = findTutorialById(endVertexId, tutorialList);

                // 验证节点是否存在，避免无效边
                if (startVertex != null && endVertex != null) {
                    DAG.addEdge(startVertex, endVertex);
                } else {
                    System.err.println("Invalid edge: " + startVertexId + " -> " + endVertexId);
                }
            }

            // 验证是否为无环图
            if (!isAcyclic()) {
                throw new IllegalStateException("The graph contains cycles and is not a valid DAG!");
            }
        } finally {
            LOCK.unlock();
        }
    }

    // 重新加载 DAG
    public static void reloadDAG() {
        initializeDAG();
    }

    // 使用拓扑排序检查是否为无环图
    private static boolean isAcyclic() {
        org.jgrapht.alg.cycle.CycleDetector<Tutorial, DefaultEdge> cycleDetector =
                new org.jgrapht.alg.cycle.CycleDetector<>(DAG);
        return !cycleDetector.detectCycles();
    }

    // 根据 ID 查找 Tutorial 对象
    private static Tutorial findTutorialById(Long id, List<Tutorial> tutorialList) {
        return tutorialList.stream()
                .filter(tutorial -> tutorial.getId().equals(id))
                .findFirst()
                .orElse(null);
    }
}
