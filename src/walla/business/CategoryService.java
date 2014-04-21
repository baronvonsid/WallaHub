package walla.business;

import java.util.*;
import walla.datatypes.auto.*;
import walla.datatypes.java.*;
import walla.db.*;
import walla.utils.*;

import javax.sql.DataSource;
import javax.xml.datatype.*;
import org.apache.log4j.Logger;
import org.springframework.http.HttpStatus;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.beans.factory.annotation.Qualifier;

@Service("CategoryService")
public class CategoryService {

	private CategoryDataHelperImpl categoryDataHelper;
	private UtilityDataHelperImpl utilityDataHelper;
	private CachedData cachedData;
	private ImageService imageService;
	private GalleryService galleryService;
	
	private static final Logger meLogger = Logger.getLogger(CategoryService.class);

	//*************************************************************************************************************
	//***********************************  Web server synchronous methods *****************************************
	//*************************************************************************************************************
	
	public long CreateCategory(long userId, Category newCategory, CustomResponse customResponse)
	{
		try {
			
			meLogger.debug("CreateCategory() begins. UserId:" + userId);
			
			//TODO Check User is logged in with Write permission
			//HttpStatus.UNAUTHORIZED.value()
			
			if (newCategory.getParentId() == 0)
			{
				String error = "CreateUpdateCategory failed, root category cannot be used in this context.";
				meLogger.error(error);
				throw new WallaException("CategoryService", "CreateCategory", error, HttpStatus.BAD_REQUEST.value());
			}
			
			//New Category
			long categoryId = utilityDataHelper.GetNewId("CategoryId");
			categoryDataHelper.CreateCategory(userId, newCategory, categoryId);
			customResponse.setResponseCode(HttpStatus.CREATED.value());
			
			//TODO decouple this method.  Don't think we need.
			//CategoryRippleUpdate(userId, categoryId);
			
			meLogger.debug("CreateCategory() has completed. UserId:" + userId + " CategoryId:" + categoryId);

			return categoryId;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process CreateCategory");
			if (wallaEx.getCustomStatus() == 0)
			{ customResponse.setResponseCode(wallaEx.getCustomStatus()); }
			else
			{ customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value()); }
			return 0;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces CreateCategory", ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return 0;
		}
	}

	public int UpdateCategory(long userId, Category newCategory, long categoryId)
	{
		try {
			
			meLogger.debug("UpdateCategory() begins. UserId:" + userId + " CategoryId:" + categoryId);
			
			//TODO Check User is logged in with Write permission
			//HttpStatus.UNAUTHORIZED.value()
			
			if (newCategory.getParentId() == 0)
			{
				String error = "UpdateCategory failed, root category cannot be updated.";
				meLogger.error(error);
				throw new WallaException("CategoryService", "UpdateCategory", error, HttpStatus.BAD_REQUEST.value());
			}
			
			if (newCategory.getId() != categoryId)
			{
				String error = "UpdateCategory failed, category Ids don't match.";
				meLogger.error(error);
				throw new WallaException("CategoryService", "UpdateCategory", error, HttpStatus.CONFLICT.value());
			}
			
			Category existingCategory = categoryDataHelper.GetCategoryMeta(userId, categoryId);
			
			if (existingCategory.getVersion() != newCategory.getVersion())
			{
				String error = "Update Category failed, record versions don't match.";
				meLogger.error(error);
				throw new WallaException("CategoryService", "UpdateCategory", error, HttpStatus.CONFLICT.value()); 
			}

			categoryDataHelper.UpdateCategory(userId, newCategory);
			
			if (existingCategory.getParentId() != newCategory.getParentId())
			{
				//TODO decouple this method.
				CategoryRippleUpdate(userId, categoryId);
			}
			
			meLogger.debug("UpdateCategory() has completed. UserId:" + userId);

			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process UpdateCategory");
			if (wallaEx.getCustomStatus() == 0)
				wallaEx.setCustomStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces UpdateCategory", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}
	
	public int DeleteCategory(long userId, Category category, long categoryId)
	{
		try {
			meLogger.debug("DeleteCategory() begins. UserId:" + userId + " CategoryName:" + categoryId);
			
			if (category.getId() != categoryId)
			{
				String error = "DeleteCategory failed, category Ids don't match.";
				meLogger.error(error);
				throw new WallaException("CategoryService", "DeleteCategory", error, HttpStatus.CONFLICT.value());
			}

			if (category.getParentId() == 0)
			{
				String error = "Delete Category failed, root category cannot be deleted.";
				meLogger.error(error);
				throw new WallaException("CategoryService", "DeleteCategory", error, HttpStatus.BAD_REQUEST.value());
			}
			
			long[] categoryIds = categoryDataHelper.GetCategoryHierachy(userId, categoryId, false);
			
			categoryDataHelper.MarkCategoryAsDeleted(userId, categoryIds, category);
			
			meLogger.debug("DeleteCategory() has completed. UserId:" + userId);
			
			//TODO decouple this method.
			CategoryRippleDelete(userId, categoryIds);
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process DeleteCategory");
			if (wallaEx.getCustomStatus() == 0)
				wallaEx.setCustomStatus(HttpStatus.INTERNAL_SERVER_ERROR.value());
			
			return wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces DeleteCategory", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}
	
	public Category GetCategoryMeta(long userId, long categoryId, CustomResponse customResponse)
	{
		try {
			//Check user can access category list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetCategoryMeta() begins. UserId:" + userId + " CategoryId:" + categoryId);
			
			//Get category list for response.
			Category category = categoryDataHelper.GetCategoryMeta(userId, categoryId);
			if (category == null)
			{
				String error = "GetCategoryMeta didn't return a valid Category object";
				meLogger.error(error);
				throw new WallaException("CategoryService", "GetCategoryMeta", error, HttpStatus.BAD_REQUEST.value()); 
			}
			
			meLogger.debug("GetCategoryMeta has completed. UserId:" + userId);
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			return category;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetCategoryMeta", wallaEx);
			customResponse.setResponseCode(wallaEx.getCustomStatus());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to process GetCategoryMeta",ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public ImageList GetCategoryWithImages(long userId, long categoryId, int imageCursor, int size, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		try
		{
			//Check user can access category
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetCategoryWithImages() begins. UserId:" + userId + " CategoryId:" + categoryId);

			ImageList categoryImageList = null;

			//Get main category for response.
			categoryImageList = categoryDataHelper.GetCategoryImageListMeta(userId, categoryId);

			//Check if category list changed
			if (clientVersionTimestamp != null)
			{
				Date lastUpdated = new Date(categoryImageList.getLastChanged().toGregorianCalendar().getTimeInMillis());
				if (!lastUpdated.after(clientVersionTimestamp))
				{
					meLogger.debug("No category images list generated because server timestamp (" + lastUpdated.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}

			//Get total count for the result set (if first request) - TODO this works for EVERY request, need to change.
			int totalImageCount = categoryDataHelper.GetTotalImageCount(userId, categoryId);
			categoryImageList.setTotalImageCount(totalImageCount);
			if (totalImageCount > 0)
			{
				categoryDataHelper.GetCategoryImages(userId, imageCursor, size, categoryImageList);
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			meLogger.debug("GetCategoryWithImages() has completed. UserId:" + userId);
			return categoryImageList;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetCategoryWithImages");
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces GetCategoryWithImages", ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public CategoryList GetCategoryListForUser(long userId, Date clientVersionTimestamp, CustomResponse customResponse)
	{
		try {
			//Check user can access category list
			//HttpStatus.UNAUTHORIZED.value()
			
			meLogger.debug("GetCategoryListForUser() begins. UserId:" + userId);
			
			CategoryList categoryList = null;
			Date lastUpdate = categoryDataHelper.LastCategoryListUpdate(userId);
			
			//lastUpdate.setTime(1000 * (lastUpdate.getTime() / 1000));
			
			//Check if category list changed
			if (clientVersionTimestamp != null)
			{
				if (!lastUpdate.after(clientVersionTimestamp) || lastUpdate.equals(clientVersionTimestamp))
				{
					meLogger.debug("No category list generated because server timestamp (" + lastUpdate.toString() + ") is not later than client timestamp (" + clientVersionTimestamp.toString() + ")");
					customResponse.setResponseCode(HttpStatus.NOT_MODIFIED.value());
					return null;
				}
			}
			
			//Get category list for response.
			categoryList = categoryDataHelper.GetUserCategoryList(userId);
			
			if (categoryList != null)
			{
				GregorianCalendar gregory = new GregorianCalendar();
				gregory.setTime(lastUpdate);
				XMLGregorianCalendar xmlOldGreg = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregory);
				
				categoryList.setLastChanged(xmlOldGreg);
			}
			
			customResponse.setResponseCode(HttpStatus.OK.value());
			
			meLogger.debug("GetCategoryListForUser has completed. UserId:" + userId);
			return categoryList;
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetCategoryListForUser");
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces GetCategoryListForUser", ex);
			customResponse.setResponseCode(HttpStatus.INTERNAL_SERVER_ERROR.value());
			return null;
		}
	}
	
	public int MoveToNewCategory(long userId, long categoryId, ImageIdList moveList)
	{
		try
		{
			//Retrieve existing category list.
			long[] categoriesAffected = categoryDataHelper.GetCategoryIdFromImageMoveList(userId, moveList);
			
			if (categoriesAffected == null)
			{
				meLogger.debug("Unexpected error, No categories were identified for update.");
				return HttpStatus.BAD_REQUEST.value();
			}
			
			//Apply category updates to db.
			categoryDataHelper.MoveImagesToNewCategory(userId, categoryId, moveList);
			
			//TODO decouple this method.
			CategoryRippleUpdate(userId, categoryId);
			
			//TODO decouple this method.
			for (int i = 0; i< categoriesAffected.length; i++)
			{
				CategoryRippleUpdate(userId, categoriesAffected[i]);
			}
			
			return HttpStatus.OK.value();
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected error when trying to process GetCategoryListForUser");
			return (wallaEx.getCustomStatus() == 0) ? HttpStatus.INTERNAL_SERVER_ERROR.value() : wallaEx.getCustomStatus();
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces GetCategoryListForUser", ex);
			return HttpStatus.INTERNAL_SERVER_ERROR.value();
		}
	}
	
	public long CreateOrFindUserAppCategory(long userId, int platformId, String machineName)
	{
		try
		{
			Platform platform = cachedData.GetPlatform(platformId, "", "", 0, 0);
			String categoryName = platform.getShortName() + " " + machineName;
			if (categoryName.length() > 30)
				categoryName = categoryName.substring(0,30);
			
			String sql = "SELECT [CategoryId] FROM [Category] WHERE [SystemOwned] = 1 AND [Name] = '" + categoryName + "' AND [UserId] = " + userId;
			long categoryId = utilityDataHelper.GetLong(sql);
			if (categoryId > 1)
			{
				return categoryId;
			}
			else
			{
				sql = "SELECT [CategoryId] FROM [Category] WHERE [ParentId] = 0 AND [UserId] = " + userId;
				long parentCategoryId = utilityDataHelper.GetLong(sql);
				
				if (parentCategoryId < 1)
				{
					String error = "Couldn't retrieve a valid parent category";
					throw new WallaException("CategoryService", "CreateOrFindUserAppCategory", error, 0); 
				}
				
				Category newCategory = new Category();
				categoryId = utilityDataHelper.GetNewId("CategoryId");
				newCategory.setName(categoryName);
				newCategory.setDesc("Auto generated tag for fotos uploaded from " + machineName + " - " + platform.getShortName());
				newCategory.setSystemOwned(true);
				newCategory.setParentId(parentCategoryId);
				
				categoryDataHelper.CreateCategory(userId, newCategory, categoryId);
				return categoryId;
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error(wallaEx);
			return 0;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return 0;
		}
	}
	
	public long FindDefaultUserCategory(long userId)
	{
		try
		{
			String sql = "SELECT MIN([CategoryId]) from Category where ParentId = (SELECT [CategoryId] FROM [Category] WHERE [ParentId] = 0 AND [UserId] = " + userId + ")";
			long categoryId = utilityDataHelper.GetLong(sql);
			if (categoryId > 1)
			{
				return categoryId;
			}
			else
			{
				String error = "Couldn't retrieve a valid default category";
				throw new WallaException("CategoryService", "FindDefaultUserCategory", error, 0); 
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error(wallaEx);
			return 0;
		}
		catch (Exception ex) {
			meLogger.error(ex);
			return 0;
		}
	}
	
	//*************************************************************************************************************
	//*************************************  Messaging initiated methods ******************************************
	//*************************************************************************************************************
	
	public void CategoryRippleDelete(long userId, long[] categoryIds)
	{
		try
		{
			/*
				Should be called when a category is deleted.
			*/

			long[] galleryIds = categoryDataHelper.GetGalleryReferencingCategory(userId, categoryIds);
			
			//TODO Post Gallery Update Timestamps
			for (int i = 0; i < galleryIds.length; i++)
			{
				//TODO decouple
				galleryService.RefreshGalleryImages(userId, galleryIds[0]);
			}
			
			imageService.DeleteAllImagesCategory(userId, categoryIds);
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected Walla error when trying to process CategoryRippleUpdates", wallaEx);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces CategoryRippleUpdates", ex);
		}
	}

	public void CategoryRippleUpdate(long userId, long categoryId)
	{
		try
		{
			/*
				Messages must be aggregated up before method is called.
				Should be called when categories are moved, or the parent is changed.
				Should be called when images are added\removed to a category.
				
				Gets a list categories which are affected by changes to this category.
			*/
			
			//Category has been updated, get all categories which might be affected.
			long[] categoryIds = categoryDataHelper.GetCategoryHierachy(userId, categoryId, true);
			
			//Update LastUpdated dates for each category traversed.
			categoryDataHelper.UpdateCategoryTimeAndCount(userId, categoryIds);
			
			long[] galleryIds = categoryDataHelper.GetGalleryReferencingCategory(userId, categoryIds);
			
			//TODO Post Gallery Update Timestamps
			for (int i = 0; i < galleryIds.length; i++)
			{
				//TODO decouple
				galleryService.RefreshGalleryImages(userId, galleryIds[0]);
			}
		}
		catch (WallaException wallaEx) {
			meLogger.error("Unexpected Walla error when trying to process CategoryRippleUpdates", wallaEx);
		}
		catch (Exception ex) {
			meLogger.error("Unexpected error when trying to proces CategoryRippleUpdates", ex);
		}
	}
	
	
	//*************************************************************************************************************
	//*************************************  Plumbing *************************************************************
	//*************************************************************************************************************
	
	
	public void setCategoryDataHelper(CategoryDataHelperImpl categoryDataHelper)
	{
		this.categoryDataHelper = categoryDataHelper;
	}
	
	public void setCachedData(CachedData cachedData)
	{
		this.cachedData = cachedData;
	}
	
	public void setUtilityDataHelper(UtilityDataHelperImpl utilityDataHelper)
	{
		this.utilityDataHelper = utilityDataHelper;
	}
	
	
	public void setImageService(ImageService imageService)
	{
		this.imageService = imageService;
	}
	
	public void setGalleryService(GalleryService galleryService)
	{
		this.galleryService = galleryService;
	}
	
}
