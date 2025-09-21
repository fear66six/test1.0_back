package com.byrski.strategy.factory;

import com.byrski.common.exception.ByrSkiException;
import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.enums.TicketType;
import com.byrski.strategy.TicketStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * 票据策略工厂类，用于管理和获取不同类型票据的处理策略。
 * <p>
 * 该类结合了工厂模式和策略模式，用于创建和管理不同类型票据的处理策略。
 * 使用 ConcurrentHashMap 存储策略实例，确保线程安全。
 * </p>
 *
 * @see TicketStrategy
 * @see TicketType
 * @see BaseTicketEntity
 */
@Component
public class TicketStrategyFactory {

    /**
     * 存储票据类型与对应策略的映射关系
     * 使用 ConcurrentHashMap 确保线程安全
     */
    private final Map<TicketType, TicketStrategy<?>> strategyMap = new ConcurrentHashMap<>();

    /**
     * 构造函数，通过依赖注入初始化策略映射。
     * 将所有实现了 TicketStrategy 接口的策略类注入到工厂中。
     *
     * @param strategies 票据策略列表，通过 Spring 自动注入所有 TicketStrategy 实现类
     */
    @Autowired
    public TicketStrategyFactory(List<TicketStrategy<?>> strategies) {
        strategies.forEach(strategy ->
                strategyMap.put(strategy.getTicketType(), strategy));
    }

    /**
     * 根据票据类型获取对应的处理策略。
     *
     * @param type 票据类型
     * @param <T>  票据实体类型，必须继承自 BaseTicketEntity
     * @return 对应的票据处理策略
     * @throws ByrSkiException 当找不到对应的票据类型策略时抛出异常
     */
    @SuppressWarnings("unchecked")
    public <T extends BaseTicketEntity> TicketStrategy<T> getStrategy(TicketType type) {
        TicketStrategy<?> strategy = strategyMap.get(type);
        if (strategy == null) {
            throw new ByrSkiException("Unsupported ticket type: " + type);
        }
        // 这里的类型转换是安全的，因为策略实现类都是针对特定票据类型的
        return (TicketStrategy<T>) strategy;
    }

    /**
     * 获取指定活动的所有票据列表
     * @param activtyId 活动ID
     * @return 指定活动的所有票据列表
     */
    public List<BaseTicketEntity> getAllTicketsByActivityId(Long activtyId) {
        return strategyMap.values().stream()
                .map(strategy -> getTicketsByActivityIdAndStrategy(strategy, activtyId))
                .flatMap(List::stream)
                .collect(Collectors.toList());
    }

    /**
     * 从特定策略获取票据列表
     *
     * @param strategy 票据策略
     * @return 票据列表
     */
    @SuppressWarnings("unchecked")
    private List<BaseTicketEntity> getTicketsByActivityIdAndStrategy(TicketStrategy<?> strategy, Long activityId) {
        // 这里需要在TicketStrategy接口中添加获取票据列表的方法
        return (List<BaseTicketEntity>) strategy.getTicketsByActivityId(activityId);
    }

    public BaseTicketEntity getTicketById(Long ticketId) {
        return (BaseTicketEntity) strategyMap.values().stream()
                .map(strategy -> getTicketByIdAndStrategy(strategy, ticketId))
                .filter(Objects::nonNull)
                .findFirst()
                .orElse(null);
    }

    private Object getTicketByIdAndStrategy(TicketStrategy<?> strategy, Long ticketId) {
        return strategy.getTicketById(ticketId);
    }
}