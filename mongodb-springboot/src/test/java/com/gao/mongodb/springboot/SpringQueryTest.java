package com.gao.mongodb.springboot;

import com.gao.mongodb.springboot.entity.Address;
import com.gao.mongodb.springboot.entity.Comment;
import com.gao.mongodb.springboot.entity.Favorites;
import com.gao.mongodb.springboot.entity.User;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.aggregation.Aggregation;
import org.springframework.data.mongodb.core.aggregation.AggregationResults;
import org.springframework.data.mongodb.core.aggregation.DateOperators;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import java.math.BigDecimal;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

import static org.springframework.data.mongodb.core.aggregation.Aggregation.*;
import static org.springframework.data.mongodb.core.query.Criteria.where;
import static org.springframework.data.mongodb.core.query.Query.query;

@RunWith(SpringJUnit4ClassRunner.class)
@SpringBootTest(classes= Application.class)
public class SpringQueryTest {

	@Autowired
	private MongoTemplate tempelate;

	//插入数据，注意salary属性，通过自定义转换器转换为Decimal128类型
	@Test
	public void insertDemo(){
		User user = new User();
		user.setUsername("cang");
		user.setCountry("USA");
		user.setAge(20);
		user.setLenght(1.77f);
		user.setSalary(new BigDecimal("6265.22"));

		//添加“address”子文档
		Address address1 = new Address();
		address1.setaCode("411222");
		address1.setAdd("sdfsdf");
		user.setAddress(address1);

		//添加“favorites”子文档，其中两个属性是数组
		Favorites favorites1 = new Favorites();
		favorites1.setCites(Arrays.asList("东莞","东京"));
		favorites1.setMovies(Arrays.asList("西游记","一路向西"));
		user.setFavorites(favorites1);


		User user1 = new User();
		user1.setUsername("chen");
		user1.setCountry("China");
		user1.setAge(30);
		user1.setLenght(1.77f);
		user1.setSalary(new BigDecimal("6885.22"));
		Address address2 = new Address();
		address2.setaCode("411000");
		address2.setAdd("我的地址2");
		user1.setAddress(address2);
		Favorites favorites2 = new Favorites();
		favorites2.setCites(Arrays.asList("珠海","东京"));
		favorites2.setMovies(Arrays.asList("东游记","一路向东"));
		user1.setFavorites(favorites2);

		tempelate.insertAll(Arrays.asList(user,user1));
	}


	// -----------------------------操作符使用实例------------------------------------------

	// db.users.find({"username":{"$in":["lison", "mark", "james"]}}).pretty()
	// 查询姓名为lison、mark和james这个范围的人
	@Test
	public void testInOper() {
		Query query = query(where("username").in("lison", "mark", "cang"));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	}


	// db.users.find({"lenght":{"$exists":true}}).pretty()
	// 判断文档有没有关心的字段
	@Test
	public void testExistsOper() {
		Query query = query(where("lenght").exists(true));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	
	}

	// db.users.find().sort({"username":1}).limit(1).skip(2)
	// 测试sort，limit，skip
	@Test
	public void testSLSOper() {
		//Query query = query(where(null)).with(new Sort(new Sort.Order(Direction.ASC, "username"))).limit(1).skip(2);
		Query query = query(where(null)).with(Sort.by(Direction.ASC, "username")).limit(1).skip(2);
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);

	}

	// db.users.find({"lenght":{"$not":{"$gte":1.77}}}).pretty()
	// 查询高度小于1.77或者没有身高的人
	// not语句 会把不包含查询语句字段的文档 也检索出来
	@Test
	public void testNotOper() {
		Query query = query(where("lenght").not().gte(1.77));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	}
	
	// -----------------------------字符串数组查询实例------------------------------------------

	// db.users.find({"favorites.movies":"蜘蛛侠"})
	// 查询数组中包含"蜘蛛侠"
	@Test
	public void testArray1() {
		Query query = query(where("favorites.movies").is("蜘蛛侠"));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	}

	// db.users.find({"favorites.movies":[ "妇联4","杀破狼2", "战狼", "雷神1","神奇动物在哪里"]},{"favorites.movies":1})
	// 查询数组等于[ “杀破狼2”, “战狼”, “雷神1” ]的文档，严格按照数量、顺序；

	@Test
	public void testArray2() {
		Query query = query(where("favorites.movies").is(Arrays.asList("妇联4","杀破狼2", "战狼", "雷神1","神奇动物在哪里")));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	}


	//数组多元素查询
	@Test
	public void testArray3() {
		// db.users.find({"favorites.movies":{"$all":[ "雷神1", "战狼"]}},{"favorites.movies":1})
		// 查询数组包含["雷神1", "战狼" ]的文档，跟顺序无关
		
		Query query = query(where("favorites.movies").all(Arrays.asList("雷神1", "战狼")));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
		
		
//		db.users.find({"favorites.movies":{"$in":[ "雷神1", "战狼" ]}},{"favorites.movies":1})
//		查询数组包含[“雷神1”, “战狼” ]中任意一个的文档，跟顺序无关，跟数量无关
		 query = query(where("favorites.movies").in(Arrays.asList("雷神1", "战狼")));
		 find = tempelate.find(query, User.class);
		 printUsers(find);
	}

	// // db.users.find({"favorites.movies.0":"妇联4"},{"favorites.movies":1})
	// 查询数组中第一个为"妇联4"的文档

	@Test
	public void testArray4() {
		Query query = query(where("favorites.movies.0").is("妇联4"));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	}

	// db.users.find({},{"favorites.movies":{"$slice":[1,2]},"favorites":1})
	// $slice可以取两个元素数组,分别表示跳过和限制的条数；

	@Test
	public void testArray5() {
		Query query = query(where(null));
		query.fields().include("favorites").slice("favorites.movies", 1, 2);
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	}

	// -----------------------------对象数组查询实例------------------------------------------
	
	
	//db.users.find({"comments":{"author":"lison6","content":"lison评论6","commentTime":ISODate("2017-06-06T00:00:00Z")}})
	//备注：对象数组精确查找
	//坑：居然和属性定义的顺序有关
	@Test
	public void testObjArray1() throws ParseException {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date commentDate = formatter.parse("2017-06-06 08:00:00");
		Comment comment = new Comment();
		comment.setAuthor("lison6");
		comment.setCommentTime(commentDate);
		comment.setContent("lison评论6");

		Query query = query(where("comments").is(comment));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	}


	//数组多元素查询
	@Test
	public void testObjArray2() {

//		查找lison1 或者 lison12评论过的user （$in查找符） 
//		db.users.find({"comments.author":{"$in":["lison1","lison12"]}}).pretty()
//		  备注：跟数量无关，跟顺序无关；
		Query query = query(where("comments.author").in(Arrays.asList("lison1","lison12")));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);

//		查找lison1 和 lison12都评论过的user
//		db.users.find({"comments.author":{"$all":["lison12","lison1"]}}).pretty()
//		 备注：跟数量有关，跟顺序无关；
		query = query(where("comments.author").all(Arrays.asList("lison1","lison12")));
		find = tempelate.find(query, User.class);
		printUsers(find);
	}
	
	private void printUsers(List<User> find) {
		for (User user : find) {
			System.out.println(user);
		}
		System.out.println(find.size());
	}
	
	
	//---------------------------------------------------------

	
	//查找lison5评语为包含“苍老师”关键字的user（$elemMatch查找符） 
	//	db.users.find({"comments":{"$elemMatch":{"author" : "lison5", "content" : { "$regex" : ".*苍老师.*"}}}})
	//备注：数组中对象数据要符合查询对象里面所有的字段，$全元素匹配，和顺序无关；
	@Test
	public void testObjArray3() throws ParseException {
//		and(where("author").is("lison5"),where("content").regex(".*苍老师.*")))
		Criteria andOperator = new Criteria().andOperator(where("author").is("lison5"),where("content").regex(".*苍老师.*"));
		Query query = query(where("comments").elemMatch(andOperator));
		List<User> find = tempelate.find(query, User.class);
		printUsers(find);
	}




	//--------------------------------------聚合查询----------------------------------
	/**
	 *  db.orders.aggregate([
	 {"$match":{ "orderTime" : { "$lt" : new Date("2015-04-03T16:00:00.000Z")}}},
	 {"$group":{"_id":{"useCode":"$useCode","month":{"$month":"$orderTime"}},"total":{"$sum":"$price"}}},
	 {"$sort":{"_id":1}}
	 ])
	 */
	@Test
	public void aggretionTest1() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date commentDate = formatter.parse("2015-04-04 00:00:00");
		Aggregation aggs =  newAggregation(
				match(where("orderTime").lt(commentDate)),
				project("useCode","price","orderTime").and(DateOperators.DateToString.dateOf("orderTime").toString("%m")).as("month"),
				group("useCode","month").sum("price").as("total"),
				sort(Sort.by(Direction.ASC,"_id"))
		);

		AggregationResults<Object> aggregate = tempelate.aggregate(aggs, "orders",	Object.class);
		List<Object> mappedResults = aggregate.getMappedResults();
		System.out.println(mappedResults);

	}


	/**
	 *
	 db.orders.aggregate([{"$match":{ "orderTime" : { "$lt" : new Date("2015-04-03T16:00:00.000Z")}}},
	 {"$unwind":"$Auditors"},
	 {"$group":{"_id":{"Auditors":"$Auditors"},"total":{"$sum":"$price"}}},
	 {"$sort":{"_id":1}}])
	 */
	@Test
	public void aggretionTest2() throws Exception {
		SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
		Date commentDate = formatter.parse("2015-04-04 00:00:00");
		Aggregation aggs =  newAggregation(
				match(where("orderTime").lt(commentDate)),
				unwind("Auditors"),
				group("Auditors").sum("price").as("total"),
				sort(Sort.by(Direction.ASC,"_id"))
		);

		AggregationResults<Object> aggregate = tempelate.aggregate(aggs, "orders",	Object.class);
		List<Object> mappedResults = aggregate.getMappedResults();
		System.out.println(mappedResults);


	}

}