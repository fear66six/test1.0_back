package com.byrski.infrastructure.repository.manager;

import com.byrski.domain.entity.BaseTicketEntity;
import com.byrski.domain.entity.dto.Product;
import com.byrski.domain.entity.dto.ticket.BusTicket;
import com.byrski.domain.entity.dto.ticket.RoomTicket;
import com.byrski.domain.entity.dto.ticket.SkiTicket;
import com.byrski.domain.enums.ProductType;
import com.byrski.infrastructure.mapper.impl.TicketMapperService;
import com.byrski.infrastructure.repository.ProductRepository;
import jakarta.annotation.Resource;
import jakarta.validation.constraints.NotNull;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ProductManager {
    @Resource
    private ProductRepository productRepository;
    @Resource
    private TicketMapperService ticketMapperService;

    // 保存 Service 实体
    public Product saveProduct(Product productEntity) {
        return productRepository.save(productEntity);
    }

    // 根据 ID 获取 Service 实体
    public Product getProductById(String id) {
        return productRepository.findById(id).orElse(null);
    }

    // 获取所有 Service 实体
    public List<Product> getAllProducts() {
        return productRepository.findAll();
    }

    public List<Product> getProductsByActivityId(Long activityId) {
        return productRepository.findProductsByActivityId(activityId);
    }

    // 删除 Service 实体
    public void deleteProductById(String id) {
        productRepository.deleteById(id);
    }

    /**
     * 根据门票列表构建产品
     * @param tickets 门票列表
     * @return 产品
     **/
    public Product buildByTickets(List<BaseTicketEntity> tickets) {
        Integer price = 0;
        Integer originalPrice = 0;
        for (BaseTicketEntity ticket : tickets) {
            price += ticket.getBaseTicket().getPrice();
            originalPrice += ticket.getBaseTicket().getOriginalPrice();
        }
        return Product.builder()
                .tickets(tickets)
                .name("套餐")
                .description("暂无")
                .type(ProductType.PACKAGE)
                .isStudent(true)
                .salesCount(0)
                .activityId(tickets.get(0).getBaseTicket().getActivityId())
                .activityTemplateId(tickets.get(0).getBaseTicket().getActivityTemplateId())
                .snowfieldId(tickets.get(0).getBaseTicket().getSnowfieldId())
                .price(price)
                .originalPrice(originalPrice)
                .deprecated(false)
                .build();
    }

    /**
     * 根据单个门票列表构建产品
     * @param ticket 门票
     * @return 产品
     */
    public Product buildByTicket(BaseTicketEntity ticket, ProductType type) {
        Integer price = ticket.getBaseTicket().getPrice();
        Integer originalPrice = ticket.getBaseTicket().getOriginalPrice();
        return Product.builder()
                .tickets(List.of(ticket))
                .name("套餐")
                .description("暂无")
                .type(type)
                .isStudent(true)
                .activityId(ticket.getBaseTicket().getActivityId())
                .activityTemplateId(ticket.getBaseTicket().getActivityTemplateId())
                .snowfieldId(ticket.getBaseTicket().getSnowfieldId())
                .price(price)
                .originalPrice(originalPrice)
                .deprecated(false)
                .build();
    }

    /**
     * 根据雪场 ID 获取产品列表
     * @param snowfieldId 雪场 ID
     * @return 产品列表
     */
    public List<Product> findProductsBySnowfieldId(Long snowfieldId) {
        return productRepository.findProductsBySnowfieldId(snowfieldId);
    }

    /**
     * 根据活动 ID 获取产品列表
     * @param activityId 活动 ID
     * @return 产品列表
     */
    public List<Product> findProductsByActivityId(Long activityId) {
        return productRepository.findProductsByActivityId(activityId);
    }

    /**
     * 获取雪场最低产品价格，需考虑是否过滤掉单雪票/单车票？
     * @param snowfieldId 雪场 ID
     * @return 最低价格
     */
    public Integer getMinProductPriceBySnowfieldId(Long snowfieldId) {
        List<Product> products = productRepository.findProductsBySnowfieldId(snowfieldId);
        return getMinPrice(products);
    }

    /**
     * 获取雪场最低产品价格，需考虑是否过滤掉单雪票/单车票？
     * @param activityIds 活动ID列表
     * @return 最低价格
     */
    public Integer getMinProductPriceByActivityIds(List<Long> activityIds) {
       //迭代调用findProductsByActivityId，将所有活动的产品合并
        List<Product> products = activityIds.stream()
                .map(this::findProductsByActivityId)
                .flatMap(List::stream)
                .toList();
        return getMinPrice(products);
    }

    /**
     * 获取某次活动最低产品价格，需考虑是否过滤掉单雪票/单车票？
     * @param activityId 活动 ID
     * @return 最低价格
     */
    public Integer getMinProductPriceByActivityId(Long activityId) {
        List<Product> products = productRepository.findProductsByActivityId(activityId);
        return getMinPrice(products);
    }

    @NotNull
    private Integer getMinPrice(List<Product> products) {
        if (products == null || products.isEmpty()) {
            return 0;
        }
        Integer minPrice = Integer.MAX_VALUE;
        for (Product product : products) {
            if (product.getDeprecated() || isBusProduct(product) || isRoomProduct(product)) {
                continue;
            }
            if (product.getPrice() < minPrice) {
                minPrice = product.getPrice();
            }
        }
        return minPrice;
    }

    /**
     * 判断是否全部为巴士票
     */
    public static boolean isBusProduct(Product product) {
        return isAllTicketsOfType(product, BusTicket.class);
    }

    /**
     * 判断是否全部为滑雪票
     */
    public static boolean isSkiProduct(Product product) {
        return isAllTicketsOfType(product, SkiTicket.class);
    }

    /**
     * 判断是否全部为房间票
     */
    public static boolean isRoomProduct(Product product) {
        return isAllTicketsOfType(product, RoomTicket.class);
    }

    /**
     * 通用的票类型检查方法
     * @param product 产品对象
     * @param ticketClass 要检查的票类型
     * @return 是否全部为指定的票类型
     */
    public static boolean isAllTicketsOfType(Product product, Class<? extends BaseTicketEntity> ticketClass) {
        if (product == null || product.getTickets() == null || product.getTickets().isEmpty()) {
            return false;
        }

        return product.getTickets()
                .stream()
                .allMatch(ticketClass::isInstance);
    }

    /**
     * 增加产品和对应票的销量
     * @param productId 产品 ID
     */
    public void addSale(String productId) {
        this.updateSalesCount(productId, 1);
    }

    /**
     * 减少产品和对应票的销量
     * @param productId 产品 ID
     */
    public void subSale(String productId) {
        this.updateSalesCount(productId, -1);
    }

    /**
     * 更新产品销量
     * @param productId 产品 ID
     * @param count 销量变化量
     */
    public void updateSalesCount(String productId, Integer count) {
        productRepository.findById(productId).ifPresent(product -> {
            product.setSalesCount(product.getSalesCount() + count);
            productRepository.save(product);
            for (BaseTicketEntity ticket : product.getTickets()) {
                ticketMapperService.addSalesCount(ticket.getBaseTicket().getId(), count);
        }});
    }

    /**
     * 获取产品中的房票
     * @param product 产品
     * @return 产品中包含的第一个房票，若不包含房票返回null
     */
    public RoomTicket getRoomTicket(Product product) {
        return (RoomTicket) product.getTickets().stream()
                .filter(RoomTicket.class::isInstance)
                .findFirst()
                .orElse(null);
    }

    public Boolean containRoom(String productId) {
        return getProductById(productId).getTickets().stream().anyMatch(ticket -> ticket instanceof RoomTicket);
    }

    /**
     * 获取产品中的巴士票
     * @param product 产品
     * @return 产品中包含的第一个巴士票，若不包含巴士票返回null
     */
    public BusTicket getBusTicket(Product product) {
        return (BusTicket) product.getTickets().stream()
                .filter(BusTicket.class::isInstance)
                .findFirst()
                .orElse(null);

    }

    /**
     * 获取产品中的滑雪票
     * @param product 产品
     * @return 产品中包含的第一个滑雪票，若不包含滑雪票返回null
     */
    public SkiTicket getSkiTicket(Product product) {
        return (SkiTicket) product.getTickets().stream()
                .filter(SkiTicket.class::isInstance)
                .findFirst()
                .orElse(null);
    }
}
