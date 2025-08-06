package com.example.nexusdoc.network;

import com.example.nexusdoc.ui.archives.model.GedDocument;
import com.example.nexusdoc.ui.archives.model.GedFolder;
import com.example.nexusdoc.ui.archives.model.ScanHistory;
import com.example.nexusdoc.ui.archives.model.UserPreferences;

import java.util.List;
import java.util.Map;

import retrofit2.Call;
import retrofit2.http.*;

public interface GedSupabaseService {

    // ==================== FOLDERS ====================

    @GET("ged_folders")
    Call<List<GedFolder>> getFolders(
            @Query("user_id") String userId,
            @Query("select") String select
    );

    @GET("ged_folders")
    Call<List<GedFolder>> getFoldersByType(
            @Query("user_id") String userId,
            @Query("type") String type,
            @Query("parent_id") String parentId,
            @Query("select") String select
    );

    @GET("ged_folders")
    Call<List<GedFolder>> getFoldersByParent(
            @Query("user_id") String userId,
            @Query("parent_id") String parentId,
            @Query("select") String select
    );

    @GET("ged_folders")
    Call<List<GedFolder>> getFolderByPath(
            @Query("user_id") String userId,
            @Query("type") String type,
            @Query("path") String path,
            @Query("select") String select
    );

    @GET("ged_folders")
    Call<List<GedFolder>> getFolderById(
            @Query("id") String folderId,
            @Query("select") String select
    );

    @GET("ged_folders")
    Call<List<GedFolder>> getRootFolders(
            @Query("user_id") String userId,
            @Query("type") String type,
            @Query("parent_id") String parentId, // "is.null"
            @Query("select") String select
    );

    @GET("ged_folders")
    Call<List<GedFolder>> searchFolders(
            @Query("user_id") String userId,
            @Query("or") String searchFilter,
            @Query("select") String select,
            @Query("limit") Integer limit
    );

    @Headers("Prefer: return=representation")
    @POST("ged_folders")
    Call<List<GedFolder>> createFolder(@Body GedFolder folder);

    @PATCH("ged_folders")
    Call<Void> updateFolder(
            @Query("id") String folderId,
            @Body Map<String, Object> updates
    );

    @DELETE("ged_folders")
    Call<Void> deleteFolder(@Query("id") String folderId);

    // ==================== DOCUMENTS ====================

    @GET("ged_documents")
    Call<List<GedDocument>> getDocuments(
            @Query("user_id") String userId,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") Integer limit,
            @Query("offset") Integer offset
    );

    @GET("ged_documents")
    Call<List<GedDocument>> getDocumentsByFolder(
            @Query("user_id") String userId,
            @Query("folder_id") String folderId,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("ged_documents")
    Call<List<GedDocument>> getFavoriteDocuments(
            @Query("user_id") String userId,
            @Query("is_favorite") boolean isFavorite,
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("ged_documents")
    Call<List<GedDocument>> getRecentDocuments(
            @Query("user_id") String userId,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") Integer limit
    );

    @POST("rpc/search_ged_documents")
    Call<List<GedDocument>> searchDocuments(@Body Map<String, Object> searchParams);


    @Headers("Prefer: return=representation")
    @POST("ged_documents")
    Call<List<GedDocument>> createDocument(@Body GedDocument document);

    @PATCH("ged_documents")
    Call<Void> updateDocument(
            @Query("id") String documentId,
            @Body Map<String, Object> updates
    );

    @DELETE("ged_documents")
    Call<Void> deleteDocument(@Query("id") String documentId);

    // ==================== SCAN HISTORY ====================

    @GET("ged_scan_history")
    Call<List<ScanHistory>> getScanHistory(
            @Query("user_id") String userId,
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") Integer limit
    );

    @GET("ged_scan_history")
    Call<List<ScanHistory>> getDocumentScanHistory(
            @Query("document_id") String documentId,
            @Query("select") String select,
            @Query("order") String order
    );


    @Headers("Prefer: return=representation")
    @POST("ged_scan_history")
    Call<List<ScanHistory>> createScanHistory(@Body ScanHistory scanHistory);

    // ==================== USER PREFERENCES ====================

    @GET("ged_user_preferences")
    Call<List<UserPreferences>> getUserPreferences(
            @Query("user_id") String userId
    );

    @Headers("Prefer: return=representation")
    @POST("ged_user_preferences")
    Call<List<UserPreferences>> createUserPreferences(@Body UserPreferences preferences);

    @PATCH("ged_user_preferences")
    Call<Void> updateUserPreferences(
            @Query("user_id") String userId,
            @Body Map<String, Object> updates
    );

    // ==================== TAGS ====================

    @GET("ged_tags")
    Call<List<Map<String, Object>>> getAllTags(
            @Query("select") String select,
            @Query("order") String order
    );

    @GET("ged_tags")
    Call<List<Map<String, Object>>> getPopularTags(
            @Query("select") String select,
            @Query("order") String order,
            @Query("limit") Integer limit
    );

    @POST("ged_tags")
    Call<Map<String, Object>> createTag(@Body Map<String, Object> tag);

    // ==================== BULK OPERATIONS ====================

    @PATCH("ged_folders")
    Call<Void> bulkUpdateFolders(
            @Query("user_id") String userId,
            @Body Map<String, Object> updates
    );

    @PATCH("ged_documents")
    Call<Void> bulkUpdateDocuments(
            @Query("user_id") String userId,
            @Body Map<String, Object> updates
    );

    @PATCH("ged_documents")
    Call<Void> bulkUpdateDocumentsByFolder(
            @Query("user_id") String userId,
            @Query("folder_id") String folderId,
            @Body Map<String, Object> updates
    );

    // ==================== STATISTICS ====================

    @POST("rpc/get_document_stats")
    Call<Map<String, Object>> getStorageStats(@Body Map<String, String> params);

    @GET("ged_folders")
    Call<List<Map<String, Object>>> getFolderStats(
            @Query("user_id") String userId,
            @Query("type") String type,
            @Query("select") String select
    );
}