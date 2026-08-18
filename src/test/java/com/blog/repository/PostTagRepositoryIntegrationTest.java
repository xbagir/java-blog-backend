package com.blog.repository;

import com.blog.service.CommentServiceImpl;
import com.blog.service.ImageServiceImpl;
import com.blog.service.PostServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.data.jdbc.DataJdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.jdbc.Sql;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@DataJdbcTest(useDefaultFilters = false)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({PostServiceImpl.class, CommentServiceImpl.class, ImageServiceImpl.class,
        PostFeedRepository.class, PostTagRepository.class, PostLikesRepository.class, PostImageRepository.class})
@Sql(scripts = {"classpath:schema-h2.sql", "classpath:data-h2.sql"},
        executionPhase = Sql.ExecutionPhase.BEFORE_TEST_CLASS)
class PostTagRepositoryIntegrationTest {

    @Autowired
    private PostTagRepository postTagRepository;

    @Test
    @Transactional
    void replaceTagsWithEmptyListClearsTags() {
        postTagRepository.replaceTags(1L, List.of());

        assertThat(postTagRepository.findTags(1L)).isEmpty();
    }

    @Test
    @Transactional
    void findTagsByPostIdsReturnsTagsGroupedByPostId() {
        Map<Long, List<String>> tags = postTagRepository.findTagsByPostIds(List.of(1L, 2L, 4L));

        assertThat(tags.get(1L)).containsExactly("tag1", "tag2");
        assertThat(tags.get(2L)).containsExactly("lalala");
        assertThat(tags.get(4L)).isNull();
    }
}
