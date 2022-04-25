package jpabook.jpashop.service;

import jpabook.jpashop.domain.Address;
import jpabook.jpashop.domain.Member;
import jpabook.jpashop.domain.Order;
import jpabook.jpashop.domain.OrderStatus;
import jpabook.jpashop.domain.item.Book;
import jpabook.jpashop.domain.item.Item;
import jpabook.jpashop.exception.NotEnoughStockException;
import jpabook.jpashop.repository.OrderRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.EntityManager;

import static org.assertj.core.api.Assertions.*;

@SpringBootTest
@Transactional
class OrderServiceTest {

    @Autowired EntityManager em;
    @Autowired OrderService orderService;
    @Autowired OrderRepository orderRepository;

    @Test
    @DisplayName("상품주문")
    public void order() throws Exception {
        // given
        Member member = createMember();

        int initStockCount = 10;
        Item item = createBook("JPA", 10000, initStockCount);

        int orderCount = 2;

        // when
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(OrderStatus.ORDER).isEqualTo(getOrder.getStatus());
        assertThat(1).isEqualTo(getOrder.getOrderItems().size());
        assertThat(item.getPrice() * orderCount).isEqualTo(getOrder.getTotalPrice());
        assertThat(initStockCount - orderCount).isEqualTo(item.getStockQuantity());
    }


    @Test
    @DisplayName("상품주문_재고수량초과")
    public void ExceededStockCount() throws Exception {
        // given
        Member member = createMember();
        Item item = createBook("JPA", 10000, 10);

        int orderCount = 11;

        // when, then
        assertThatThrownBy(() -> orderService.order(member.getId(), item.getId(), orderCount))
                .isInstanceOf(NotEnoughStockException.class)
                .hasMessageContaining("need more stock");
    }
    
    @Test
    @DisplayName("주문취소")
    public void orderCancel() throws Exception {
        // given
        Member member = createMember();

        int initStockCount = 10;
        Item item = createBook("JPA", 10000, initStockCount);

        int orderCount = 2;
        Long orderId = orderService.order(member.getId(), item.getId(), orderCount);

        // when
        orderService.cancelOrder(orderId);

        // then
        Order getOrder = orderRepository.findOne(orderId);

        assertThat(OrderStatus.CANCEL).isEqualTo(getOrder.getStatus());
        assertThat(initStockCount).isEqualTo(item.getStockQuantity());
    }
    
    private Book createBook(String name, int price, int stockQuantity) {
        Book book = new Book();
        book.setName(name);
        book.setPrice(price);
        book.setStockQuantity(stockQuantity);
        em.persist(book);
        return book;
    }

    private Member createMember() {
        Member member = new Member();
        member.setName("회원1");
        member.setAddress(new Address("서울", "증가로", "123-123"));
        em.persist(member);
        return member;
    }
}