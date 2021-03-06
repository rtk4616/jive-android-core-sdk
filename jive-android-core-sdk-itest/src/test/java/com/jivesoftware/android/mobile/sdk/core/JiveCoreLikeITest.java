package com.jivesoftware.android.mobile.sdk.core;

import com.google.common.base.Preconditions;
import com.jivesoftware.android.mobile.sdk.entity.ContentBodyEntity;
import com.jivesoftware.android.mobile.sdk.entity.ContentEntity;
import com.jivesoftware.android.mobile.sdk.entity.EntityUtils;
import com.jivesoftware.android.mobile.sdk.entity.value.JiveCoreObjectType;
import org.apache.http.entity.mime.content.FileBody;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import javax.annotation.Nonnull;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import static com.jivesoftware.android.mobile.sdk.entity.matcher.ContentEntityMatchers.contentLikeCount;
import static org.hamcrest.MatcherAssert.assertThat;

public class JiveCoreLikeITest extends AbstractITest {

    @Nonnull
    private ContentEntity contentEntity;

    @Nonnull
    private String contentEntityRef;

    @Nonnull
    private String contentLikeRef;

    @SuppressWarnings("ConstantConditions")
    @Before
    public void setUp() throws Exception {
        String uuid = UUID.randomUUID().toString();
        String content = "Hello world!";
        String subject = "contentLiked " + uuid;
        String contentType = "text/html";

        ContentEntity documentEntity = new ContentEntity();
        documentEntity.type = JiveCoreObjectType.document;
        documentEntity.subject = subject;
        documentEntity.content = new ContentBodyEntity();
        documentEntity.content.text = content ;
        documentEntity.content.type = contentType;

        contentEntity = jiveCoreAdmin.createContent(documentEntity, Collections.<FileBody>emptyList()).call();
        Preconditions.checkNotNull(contentEntity);
        contentEntityRef = Preconditions.checkNotNull(EntityUtils.getSelfResourceRef(contentEntity));
        contentLikeRef = Preconditions.checkNotNull(EntityUtils.getResourceRef(contentEntity, "likes"));
    }

    @Test
    @Ignore // Disabled due to JIVE-56573
    public void contentInitiallyUnliked() throws IOException {
        assertThat(contentEntity, contentLikeCount(0));
    }

    @Test
    @Ignore // Disabled due to JIVE-56573
    public void contentLike() throws Exception {
        jiveCoreUser2.likeContent(contentLikeRef).call();
        contentEntity = jiveCoreAdmin.fetchContent(contentEntityRef, new JiveCoreRequestOptions()).call();
        assertThat(contentEntity, contentLikeCount(1));
    }

    @Test
    @Ignore // Disabled due to JIVE-56573
    public void contentUnlike() throws Exception {
        jiveCoreUser2.likeContent(contentLikeRef).call();
        jiveCoreUser2.unlikeContent(contentLikeRef).call();
        contentEntity = jiveCoreAdmin.fetchContent(contentEntityRef, new JiveCoreRequestOptions()).call();
        assertThat(contentEntity, contentLikeCount(0));
    }

}
