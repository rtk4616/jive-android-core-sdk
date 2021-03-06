package com.jivesoftware.android.mobile.sdk.core;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonNode;
import com.jivesoftware.android.mobile.sdk.entity.ActivityListEntity;
import com.jivesoftware.android.mobile.sdk.entity.AnalyticsRequestEntity;
import com.jivesoftware.android.mobile.sdk.entity.AttendanceEntity;
import com.jivesoftware.android.mobile.sdk.entity.BatchRequestEntity;
import com.jivesoftware.android.mobile.sdk.entity.BatchResponseEntity;
import com.jivesoftware.android.mobile.sdk.entity.ContentEntity;
import com.jivesoftware.android.mobile.sdk.entity.ContentListEntity;
import com.jivesoftware.android.mobile.sdk.entity.ImageEntity;
import com.jivesoftware.android.mobile.sdk.entity.ImageListEntity;
import com.jivesoftware.android.mobile.sdk.entity.JiveObjectEntity;
import com.jivesoftware.android.mobile.sdk.entity.ListEntity;
import com.jivesoftware.android.mobile.sdk.entity.MemberEntity;
import com.jivesoftware.android.mobile.sdk.entity.MemberListEntity;
import com.jivesoftware.android.mobile.sdk.entity.MetadataObjectEntity;
import com.jivesoftware.android.mobile.sdk.entity.MetadataPropertyEntity;
import com.jivesoftware.android.mobile.sdk.entity.ModerationEntity;
import com.jivesoftware.android.mobile.sdk.entity.NewMemberEntity;
import com.jivesoftware.android.mobile.sdk.entity.NewsEntity;
import com.jivesoftware.android.mobile.sdk.entity.PersonEntity;
import com.jivesoftware.android.mobile.sdk.entity.PersonListEntity;
import com.jivesoftware.android.mobile.sdk.entity.PersonRolesEntity;
import com.jivesoftware.android.mobile.sdk.entity.PlaceEntity;
import com.jivesoftware.android.mobile.sdk.entity.PlaceListEntity;
import com.jivesoftware.android.mobile.sdk.entity.PollVoteListEntity;
import com.jivesoftware.android.mobile.sdk.entity.StreamEntity;
import com.jivesoftware.android.mobile.sdk.entity.StreamListEntity;
import com.jivesoftware.android.mobile.sdk.entity.TokenEntity;
import com.jivesoftware.android.mobile.sdk.entity.VoteEntity;
import com.jivesoftware.android.mobile.sdk.httpclient.JiveCoreHttpClientAuthUtils;
import com.jivesoftware.android.mobile.sdk.json.JiveJson;
import com.jivesoftware.android.mobile.sdk.parser.JiveCoreExceptionFactory;
import com.jivesoftware.android.mobile.sdk.util.HttpClientUtil;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.util.List;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.methods.HttpPut;
import org.apache.http.client.methods.HttpRequestBase;
import org.apache.http.entity.mime.content.AbstractContentBody;
import org.apache.http.impl.client.AbstractHttpClient;

@ParametersAreNonnullByDefault
public class JiveCore implements Closeable {
    static {
        // Uncomment to turn on HttpClient Wire Debugging
        //noinspection ConstantConditions,ConstantIfStatement
        if (false) {
            java.util.logging.Logger.getLogger("org.apache.http.wire").setLevel(java.util.logging.Level.FINEST);

            System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.SimpleLog");
            System.setProperty("org.apache.commons.logging.simplelog.showdatetime", "true");
            System.setProperty("org.apache.commons.logging.simplelog.log.httpclient.wire", "debug");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http", "debug");
            System.setProperty("org.apache.commons.logging.simplelog.log.org.apache.http.headers", "debug");
        }

        // also run the following in adb
        // adb shell setprop log.tag.org.apache.http VERBOSE
        // adb shell setprop log.tag.org.apache.http.wire VERBOSE
        // adb shell setprop log.tag.org.apache.http.headers VERBOSE
    }

    private final TypeReference<ListEntity<ModerationEntity>> MODERATION_LIST_TYPE_REF = new TypeReference<ListEntity<ModerationEntity>>() {
    };

    @Nonnull
    public final JiveCoreRequestFactory jiveCoreRequestFactory;
    @Nonnull
    private final HttpClient httpClient;
    @Nonnull
    private final JiveCoreJiveJsonCallableFactory jiveCoreJiveJsonCallableFactory;
    @Nonnull
    private final JiveCoreEmptyCallableFactory jiveCoreEmptyCallableFactory;
    @Nonnull
    private final JiveCoreInputStreamCallableFactory jiveCoreInputStreamCallableFactory;
    @Nonnull
    private final JiveCoreGenericCallableFactory jiveCoreGenericCallableFactory;

    public JiveCore(
            URL baseURL,
            String oauthCredentials,
            AbstractHttpClient httpClient,
            JiveCoreTokenEntityStore tokenEntityStore,
            JiveCoreTokenEntityRefresher tokenEntityRefresher,
            @Nullable JiveCoreJiveClientProvider jiveClientProvider,
            JiveJson jiveJson) {
        this(new JiveCoreRequestFactory(
                        oauthCredentials,
                        baseURL,
                        jiveJson),
                JiveCoreHttpClientAuthUtils.initHttpClientAuth(
                        httpClient,
                        tokenEntityStore,
                        tokenEntityRefresher,
                        jiveClientProvider),
                jiveJson);
    }

    public JiveCore(
            JiveCoreRequestFactory jiveCoreRequestFactory,
            HttpClient httpClient,
            JiveJson jiveJson) {
        this(jiveCoreRequestFactory, httpClient, jiveJson, new JiveCoreExceptionFactory(jiveJson));
    }

    public JiveCore(
            JiveCoreRequestFactory jiveCoreRequestFactory,
            HttpClient httpClient,
            JiveJson jiveJson,
            JiveCoreExceptionFactory jiveCoreExceptionFactory) {
        this(
                httpClient, jiveCoreRequestFactory,
                new JiveCoreJiveJsonCallableFactory(httpClient, jiveJson, jiveCoreExceptionFactory),
                new JiveCoreEmptyCallableFactory(httpClient, jiveCoreExceptionFactory),
                new JiveCoreInputStreamCallableFactory(httpClient, jiveCoreExceptionFactory),
                new JiveCoreGenericCallableFactory(httpClient, jiveCoreExceptionFactory));
    }

    public JiveCore(
            HttpClient httpClient,
            JiveCoreRequestFactory jiveCoreRequestFactory,
            JiveCoreJiveJsonCallableFactory jiveCoreJiveJsonCallableFactory,
            JiveCoreEmptyCallableFactory jiveCoreEmptyCallableFactory,
            JiveCoreInputStreamCallableFactory jiveCoreInputStreamCallableFactory,
            JiveCoreGenericCallableFactory jiveCoreGenericCallableFactory) {
        this.httpClient = httpClient;
        this.jiveCoreRequestFactory = jiveCoreRequestFactory;
        this.jiveCoreJiveJsonCallableFactory = jiveCoreJiveJsonCallableFactory;
        this.jiveCoreEmptyCallableFactory = jiveCoreEmptyCallableFactory;
        this.jiveCoreInputStreamCallableFactory = jiveCoreInputStreamCallableFactory;
        this.jiveCoreGenericCallableFactory = jiveCoreGenericCallableFactory;
    }

    @Override
    public void close() throws IOException {
        HttpClientUtil.shutdownSafely(httpClient);
    }

    @Nonnull
    public JiveCoreURIFactory getURIFactory() {
        return jiveCoreRequestFactory.getURIFactory();
    }

    @Nonnull
    public JiveCoreCallable<MetadataPropertyEntity[]> fetchMetadataProperties() {
        HttpGet get = jiveCoreRequestFactory.fetchMetadataProperties();
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(get, MetadataPropertyEntity[].class);
    }

    @Nonnull
    public JiveCoreCallable<PersonEntity> fetchMePerson() {
        HttpGet fetchMePersonHttpGet = jiveCoreRequestFactory.fetchMePerson();
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchMePersonHttpGet, PersonEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonRolesEntity> fetchPersonRoles(String requestPathAndQuery) {
        HttpGet httpGet = jiveCoreRequestFactory.fetchPersonRoles(requestPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(httpGet, PersonRolesEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<NewsEntity> fetchMeNews() {
        HttpGet fetchMeNewsHttpGet = jiveCoreRequestFactory.fetchMeNews();
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchMeNewsHttpGet, NewsEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ActivityListEntity> fetchTrending(JiveCoreQueryParameterProvider options) {
        HttpGet fetchTrendingNewsHttpGet = jiveCoreRequestFactory.fetchTrending(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchTrendingNewsHttpGet, ActivityListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ListEntity<ModerationEntity>> fetchModerationList(JiveCoreRequestOptions options) {
        HttpGet fetchModerationListHttpGet = jiveCoreRequestFactory.fetchModerationPending(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchModerationListHttpGet, MODERATION_LIST_TYPE_REF);
    }

    @Nonnull
    public JiveCoreCallable<ListEntity<ModerationEntity>> fetchMoreModerationList(String requestPathAndQuery) {
        HttpGet fetchModerationListHttpGet = jiveCoreRequestFactory.createHttpGet(requestPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchModerationListHttpGet, MODERATION_LIST_TYPE_REF);
    }

    @Nonnull
    public JiveCoreCallable<JsonNode> fetchAnalyticsData(String requestPathAndQuery, AnalyticsRequestEntity analyticsRequestEntity) {
        HttpPost httpPost = jiveCoreRequestFactory.fetchAnalytics(requestPathAndQuery, analyticsRequestEntity);

        return jiveCoreJiveJsonCallableFactory.createGsonCallable(httpPost, JsonNode.class);
    }

    @Nonnull
    public JiveCoreCallable<StreamEntity> fetchStream(String streamsPathAndQuery) {
        HttpGet fetchStreamsHttpGet = jiveCoreRequestFactory.createHttpGet(streamsPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchStreamsHttpGet, StreamEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<StreamListEntity> fetchStreams(String streamsPathAndQuery) {
        HttpGet fetchStreamsHttpGet = jiveCoreRequestFactory.createHttpGet(streamsPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchStreamsHttpGet, StreamListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<StreamListEntity> fetchMeStreams() {
        HttpGet fetchStreamsHttpGet = jiveCoreRequestFactory.fetchMeStreams();
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchStreamsHttpGet, StreamListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ActivityListEntity> fetchActivities(String requestPathAndQuery) {
        HttpGet fetchActivitiesHttpGet = jiveCoreRequestFactory.createHttpGet(requestPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchActivitiesHttpGet, ActivityListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ActivityListEntity> fetchActivities(String requestPathAndQuery, JiveCoreQueryParameterProvider options) {
        HttpGet fetchActivitiesHttpGet = jiveCoreRequestFactory.fetchActivity(requestPathAndQuery, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchActivitiesHttpGet, ActivityListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ActivityListEntity> fetchActivitiesByPerson(String personUrl, JiveCoreQueryParameterProvider options) {
        HttpGet fetchActivitiesByPerson = jiveCoreRequestFactory.fetchActivityByPerson(personUrl, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchActivitiesByPerson, ActivityListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ActivityListEntity> fetchInbox(JiveCoreQueryParameterProvider options) {
        HttpGet fetchInboxHttpGet = jiveCoreRequestFactory.fetchInbox(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchInboxHttpGet, ActivityListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonListEntity> fetchPeople(JiveCoreQueryParameterProvider options) {
        HttpGet searchPeopleHttpGet = jiveCoreRequestFactory.fetchPeople(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchPeopleHttpGet, PersonListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonListEntity> fetchRecentPeople(JiveCoreQueryParameterProvider options) {
        HttpGet searchRecentPeopleHttpGet = jiveCoreRequestFactory.fetchRecentPeople(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchRecentPeopleHttpGet, PersonListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PlaceListEntity> fetchRecentPlaces(JiveCoreQueryParameterProvider options) {
        HttpGet searchRecentPlacesHttpGet = jiveCoreRequestFactory.fetchRecentPlaces(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchRecentPlacesHttpGet, PlaceListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentListEntity> fetchRecentContent(JiveCoreQueryParameterProvider options) {
        HttpGet searchRecentContentHttpGet = jiveCoreRequestFactory.fetchRecentContent(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchRecentContentHttpGet, ContentListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonListEntity> fetchPeople(String pathAndQuery) {
        HttpGet fetchPeopleHttpGet = jiveCoreRequestFactory.createHttpGet(pathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchPeopleHttpGet, PersonListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonListEntity> fetchPeople(String pathAndQuery, JiveCoreQueryParameterProvider options) {
        HttpGet fetchPeopleHttpGet = jiveCoreRequestFactory.fetchPeople(pathAndQuery, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchPeopleHttpGet, PersonListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ActivityListEntity> fetchAllActivities() {
        HttpGet fetchActivitiesHttpGet = jiveCoreRequestFactory.fetchActivity();
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchActivitiesHttpGet, ActivityListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<Void> markInboxEntryAsRead(String markReadPathAndQuery) {
        return simplePost(markReadPathAndQuery);
    }

    @Nonnull
    public JiveCoreCallable<Void> markInboxEntryAsUnread(String markUnreadPathAndQuery) {
        return simpleDelete(markUnreadPathAndQuery);
    }

    @Nonnull
    public JiveCoreCallable<BatchResponseEntity[]> executeBatchOperation(BatchRequestEntity[] requestEntities) {
        HttpPost executeBatchOperationHttpPost = jiveCoreRequestFactory.executeBatchOperation(requestEntities);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(executeBatchOperationHttpPost, BatchResponseEntity[].class);
    }

    @Nonnull
    public JiveCoreCallable<ContentListEntity> searchContents(String requestPathAndQuery) {
        HttpGet searchContentsHttpGet = jiveCoreRequestFactory.createHttpGet(requestPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchContentsHttpGet, ContentListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentListEntity> fetchContents(JiveCoreQueryParameterProvider options) {
        HttpGet fetchContentsHttpGet = jiveCoreRequestFactory.fetchContents(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchContentsHttpGet, ContentListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentListEntity> fetchContents(String pathAndQuery) {
        HttpGet fetchContentsHttpGet = jiveCoreRequestFactory.createHttpGet(pathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchContentsHttpGet, ContentListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentListEntity> searchContents(JiveCoreQueryParameterProvider options) {
        HttpGet searchContentsHttpGet = jiveCoreRequestFactory.searchContents(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchContentsHttpGet, ContentListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonListEntity> searchPeople(String requestPathAndQuery) {
        HttpGet searchPeopleHttpGet = jiveCoreRequestFactory.createHttpGet(requestPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchPeopleHttpGet, PersonListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonListEntity> searchPeople(JiveCoreQueryParameterProvider options) {
        HttpGet searchPeopleHttpGet = jiveCoreRequestFactory.searchPeople(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchPeopleHttpGet, PersonListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonListEntity> searchRecentPeople(String requestPathAndQuery) {
        HttpGet searchPeopleHttpGet = jiveCoreRequestFactory.createHttpGet(requestPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchPeopleHttpGet, PersonListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PlaceListEntity> searchPlaces(JiveCoreQueryParameterProvider options) {
        HttpGet searchPlacesHttpGet = jiveCoreRequestFactory.searchPlaces(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchPlacesHttpGet, PlaceListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PlaceListEntity> searchPlaces(String requestPathAndQuery) {
        HttpGet searchPlacesHttpGet = jiveCoreRequestFactory.createHttpGet(requestPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(searchPlacesHttpGet, PlaceListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonEntity> fetchPerson(String personPathAndQuery) {
        HttpGet fetchPersonHttpGet = jiveCoreRequestFactory.createHttpGet(personPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchPersonHttpGet, PersonEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PersonEntity> updatePerson(PersonEntity personEntity) {
        HttpPut updatePersonHttpPut = jiveCoreRequestFactory.updatePerson(personEntity);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(updatePersonHttpPut, PersonEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PlaceEntity> fetchPlace(String placePathAndQuery) {
        HttpGet fetchPlaceHttpGet = jiveCoreRequestFactory.createHttpGet(placePathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchPlaceHttpGet, PlaceEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PlaceListEntity> fetchPlaces(JiveCoreQueryParameterProvider options) {
        HttpGet fetchContentsHttpGet = jiveCoreRequestFactory.fetchPlaces(options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchContentsHttpGet, PlaceListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PlaceListEntity> fetchPlaces(String pathAndQuery) {
        HttpGet fetchContentsHttpGet = jiveCoreRequestFactory.createHttpGet(pathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchContentsHttpGet, PlaceListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PlaceEntity> createPlace(PlaceEntity placeEntity) {
        HttpPost createPlaceHttpPost = jiveCoreRequestFactory.createPlace(placeEntity);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(createPlaceHttpPost, PlaceEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<Void> deletePlace(String pathAndQuery) {
        return simpleDelete(pathAndQuery);
    }

    @Nonnull
    public JiveCoreCallable<MemberListEntity> fetchMembersByPerson(String personID, JiveCoreQueryParameterProvider options) {
        HttpGet fetchMembersByPersonHttpGet = jiveCoreRequestFactory.fetchMembersByPerson(personID, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchMembersByPersonHttpGet, MemberListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<MemberListEntity> fetchMembersByPlace(String placeID, JiveCoreQueryParameterProvider options) {
        HttpGet fetchMembersByPlaceHttpGet = jiveCoreRequestFactory.fetchMembersByPlace(placeID, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchMembersByPlaceHttpGet, MemberListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<MemberListEntity> fetchMembers(String pathAndQuery) {
        HttpGet fetchMembersHttpGet = jiveCoreRequestFactory.createHttpGet(pathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchMembersHttpGet, MemberListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<MemberEntity> createMember(String placeID, NewMemberEntity newMemberEntity) {
        HttpPost createMembershipHttpPost = jiveCoreRequestFactory.createMembership(placeID, newMemberEntity);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(createMembershipHttpPost, MemberEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<Void> deleteMember(String memberPathAndQuery) {
        HttpDelete deleteMemberHttpDelete = jiveCoreRequestFactory.createHttpDelete(memberPathAndQuery);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(deleteMemberHttpDelete);
    }

    @Nonnull
    public JiveCoreCallable<MetadataObjectEntity> fetchMetadataObject(String metadataObjectName, String locale) {
        HttpGet fetchMetadataObjectHttpGet = jiveCoreRequestFactory.fetchMetadataObject(metadataObjectName, locale);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchMetadataObjectHttpGet, MetadataObjectEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<Void> likeContent(String likePathAndQuery) {
        return simplePost(likePathAndQuery);
    }

    @Nonnull
    public JiveCoreCallable<Void> unlikeContent(String unlikePathAndQuery) {
        return simpleDelete(unlikePathAndQuery);
    }

    @Nonnull
    public JiveCoreCallable<Void> promoteContent(String promoteRequestUrl, VoteEntity voteEntity) {
        HttpPost promoteContentHttpPost = jiveCoreRequestFactory.createPromoteContent(promoteRequestUrl, voteEntity);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(promoteContentHttpPost);
    }

    /**
     * @deprecated use registerForMobile3Push or registerForMobile4Push instead.
     */
    @Nonnull
    @Deprecated
    public JiveCoreCallable<Void> registerForPush(String gcmId, String deviceId) {
        return registerForMobile3Push(gcmId, deviceId);
    }

    @Nonnull
    public JiveCoreCallable<Void> registerForMobile3Push(String gcmId, String deviceId) {
        HttpPost registerForPushHttpPost = jiveCoreRequestFactory.registerForMobile3Push(gcmId, deviceId);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(registerForPushHttpPost);
    }

    @Nonnull
    public JiveCoreCallable<Void> registerForMobile4Push(String gcmId, String deviceId) {
        HttpPost registerForPushHttpPost = jiveCoreRequestFactory.registerForMobile4Push(gcmId, deviceId);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(registerForPushHttpPost);
    }

    @Nonnull
    public JiveCoreCallable<Void> unregisterFromPush(String gcmId, String deviceId) {
        HttpPost unregisterFromPushHttpPost = jiveCoreRequestFactory.unregisterFromPush(gcmId, deviceId);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(unregisterFromPushHttpPost);
    }

    @Nonnull
    public JiveCoreCallable<ContentEntity> createContent(
            ContentEntity contentEntity, List<? extends AbstractContentBody> fileBodies) {
        HttpPost createContentHttpPost = jiveCoreRequestFactory.createContent(contentEntity, fileBodies);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(createContentHttpPost, ContentEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentEntity> createContent(
            String pathAndQuery, ContentEntity contentEntity, List<? extends AbstractContentBody> fileBodies) {
        HttpPost createContentHttpPost = jiveCoreRequestFactory.createContent(pathAndQuery, contentEntity, fileBodies);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(createContentHttpPost, ContentEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ModerationEntity> updateModeration(ModerationEntity moderationEntity) {
        HttpPut updateModerationPut = jiveCoreRequestFactory.updateModeration(moderationEntity);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(updateModerationPut, ModerationEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<AttendanceEntity> createRsvp(String eventRsvpPathAndQuery, String rsvpVal) {
        HttpPost createRsvpPost = jiveCoreRequestFactory.createRsvp(eventRsvpPathAndQuery, rsvpVal);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(createRsvpPost, AttendanceEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentEntity> fetchContent(String pathAndQuery, JiveCoreQueryParameterProvider options) {
        HttpGet fetchContentHttpGet = jiveCoreRequestFactory.fetchContent(pathAndQuery, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchContentHttpGet, ContentEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentListEntity> fetchContents(String pathAndQuery, JiveCoreQueryParameterProvider options) {
        HttpGet httpGet = jiveCoreRequestFactory.fetchContent(pathAndQuery, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(httpGet, ContentListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentEntity> updateContent(
            ContentEntity contentEntity, List<? extends AbstractContentBody> fileBodies) {
        HttpPut updateContentHttpPut = jiveCoreRequestFactory.updateContent(contentEntity, fileBodies);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(updateContentHttpPut, ContentEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ContentListEntity> fetchReplies(String pathAndQuery, JiveCoreQueryParameterProvider options) {
        HttpGet fetchRepliesHttpGet = jiveCoreRequestFactory.fetchReplies(pathAndQuery, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchRepliesHttpGet, ContentListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ImageListEntity> fetchImages(String pathAndQuery) {
        HttpGet fetchImagesHttpGet = jiveCoreRequestFactory.createHttpGet(pathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchImagesHttpGet, ImageListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<ImageEntity> uploadImage(AbstractContentBody imageFileBody) {
        HttpPost uploadImageHttpPost = jiveCoreRequestFactory.uploadImage(imageFileBody);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(uploadImageHttpPost, ImageEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<InputStream> fetchImage(String pathAndQuery, JiveCoreQueryParameterProvider options) {
        HttpGet fetchImageHttpGet = jiveCoreRequestFactory.fetchImage(pathAndQuery, options);
        return jiveCoreInputStreamCallableFactory.createInputStreamCallable(fetchImageHttpGet);
    }

    @Nonnull
    public JiveCoreCallable<StreamListEntity> updateFollowingInEntitiesForObject(JiveObjectEntity objectEntity, List<StreamEntity> activityListEntity) {
        HttpPost updateFollowingInHttpPost = jiveCoreRequestFactory.updateFollowingIn(objectEntity, activityListEntity);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(updateFollowingInHttpPost, StreamListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<StreamListEntity> updateFollowingInEntitiesForUrl(String url, List<StreamEntity> streamEntities) {
        HttpPost updateFollowingInHttpPost = jiveCoreRequestFactory.updateFollowingIn(url, streamEntities);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(updateFollowingInHttpPost, StreamListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<PollVoteListEntity> fetchPollVotes(String pathAndQuery, JiveCoreQueryParameterProvider options) {
        HttpGet httpRequest = jiveCoreRequestFactory.fetchPollVotes(pathAndQuery, options);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(httpRequest, PollVoteListEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<Void> createVote(String pathAndQuery, List<String> pollOptions) {
        HttpPost httpRequest = jiveCoreRequestFactory.createVote(pathAndQuery, pollOptions);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(httpRequest);
    }

    @Nonnull
    public <E extends ListEntity> JiveCoreCallable<E> fetchList(String requestPathAndQuery, Class<E> listEntityClass) {
        HttpGet fetchListHttpGet = jiveCoreRequestFactory.createHttpGet(requestPathAndQuery);
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(fetchListHttpGet, listEntityClass);
    }

    @Nonnull
    public JiveCoreCallable<Void> completeMission(String mission) {
        HttpPost completeMissionHttpPost = jiveCoreRequestFactory.completeMission(mission);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(completeMissionHttpPost);
    }

    @Nonnull
    public <T> JiveCoreCallable<T> createCallable(HttpRequestBase httpRequestBase, HttpResponseParserFactory<T> httpResponseParserFactory) {
        return jiveCoreGenericCallableFactory.createGenericCallable(httpRequestBase, httpResponseParserFactory);
    }

    @Nonnull
    public JiveCoreCallable<TokenEntity> authorizeDeviceFromSession() {
        HttpPost authorizeDeviceFromSessionHttpPost = jiveCoreRequestFactory.authorizeDeviceFromSession();
        return jiveCoreJiveJsonCallableFactory.createGsonCallable(authorizeDeviceFromSessionHttpPost, TokenEntity.class);
    }

    @Nonnull
    public JiveCoreCallable<Void> deauthorizeDevice() {
        HttpPost deauthorizeDeviceHttpPost = jiveCoreRequestFactory.deauthorizeDevice();
        return jiveCoreEmptyCallableFactory.createEmptyCallable(deauthorizeDeviceHttpPost);
    }

    @Nonnull
    public JiveCoreCallable<Void> simplePost(String requestPathAndQuery) {
        HttpPost httpPost = jiveCoreRequestFactory.createHttpPost(requestPathAndQuery);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(httpPost);
    }

    @Nonnull
    public JiveCoreCallable<Void> simplePut(String requestPathAndQuery) {
        HttpPut httpPut = jiveCoreRequestFactory.createHttpPut(requestPathAndQuery);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(httpPut);
    }

    @Nonnull
    public JiveCoreCallable<Void> simpleDelete(String requestPathAndQuery) {
        HttpDelete httpDelete = jiveCoreRequestFactory.createHttpDelete(requestPathAndQuery);
        return jiveCoreEmptyCallableFactory.createEmptyCallable(httpDelete);
    }

}
