'use strict';

describe('service [Model]', function() {
	
	var account, feedback, nomination, model;
	var deferred, scope;
	
	beforeEach(module('feedbacker.services'));

	beforeEach(inject(function($q, _Model_, _Account_, _Feedback_, _Nomination_, $rootScope) {
		scope = $rootScope.$new();
		deferred = $q.defer();
		model = _Model_;

    account = _Account_;
    spyOn(account, 'getCurrentUser').and.returnValue(deferred.promise)
    spyOn(account, 'updateCurrentUser').and.returnValue(deferred.promise)
    spyOn(account, 'getActiveUsers').and.returnValue(deferred.promise)
    spyOn(account, 'getRegisteredUsers').and.returnValue(deferred.promise)
    spyOn(account, 'getUserReports').and.returnValue(deferred.promise)

		feedback = _Feedback_;
    spyOn(feedback, 'getPendingFeedbackActions').and.returnValue(deferred.promise);
    spyOn(feedback, 'getCurrentFeedbackItemsForSelf').and.returnValue(deferred.promise);
    spyOn(feedback, 'getFeedbackHistoryForSelf').and.returnValue(deferred.promise);
    spyOn(feedback, 'getFeedbackItem').and.returnValue(deferred.promise);
    spyOn(feedback, 'getActiveFeedbackCycles').and.returnValue(deferred.promise);
    spyOn(feedback, 'getAllFeedbackCycles').and.returnValue(deferred.promise);
    spyOn(feedback, 'getFeedbackCycle').and.returnValue(deferred.promise);
    spyOn(feedback, 'createAdHocFeedback').and.returnValue(deferred.promise);
    spyOn(feedback, 'getAdHocFeedbackForUser').and.returnValue(deferred.promise);
    spyOn(feedback, 'getAdHocFeedbackForSelf').and.returnValue(deferred.promise);
    spyOn(feedback, 'getAdHocFeedbackFromSelf').and.returnValue(deferred.promise);
    spyOn(feedback, 'createFeedbackCycle').and.returnValue(deferred.promise);
    spyOn(feedback, 'updateFeedbackCycle').and.returnValue(deferred.promise);
    spyOn(feedback, 'getCycleReports').and.returnValue(deferred.promise)
    spyOn(feedback, 'updateFeedback').and.returnValue(deferred.promise)

    nomination = _Nomination_;
    spyOn(nomination, 'getCurrentNominations').and.returnValue(deferred.promise);
	}));

	it('can get an instance of itself', function(){
    	expect(model).toBeDefined();
  });
    
  it('has defined functions', function() {
      expect(angular.isFunction(model.flush)).toBe(true);
      expect(angular.isFunction(model.getCurrentUser)).toBe(true);
      expect(angular.isFunction(model.getUserReports)).toBe(true);
      expect(angular.isFunction(model.getCycleReports)).toBe(true);
      expect(angular.isFunction(model.getPendingFeedbackActions)).toBe(true);
      expect(angular.isFunction(model.updateCurrentUser)).toBe(true);
      expect(angular.isFunction(model.getCurrentFeedback)).toBe(true);
      expect(angular.isFunction(model.getFeedbackHistory)).toBe(true);
      expect(angular.isFunction(model.getFeedbackDetail)).toBe(true);
      expect(angular.isFunction(model.updateFeedbackDetail)).toBe(true);
      expect(angular.isFunction(model.getActiveUsers)).toBe(true);
      expect(angular.isFunction(model.getRegisteredUsers)).toBe(true);
      expect(angular.isFunction(model.getCurrentNominations)).toBe(true);
      expect(angular.isFunction(model.getActiveFeedbackCycles)).toBe(true);
      expect(angular.isFunction(model.getAllFeedbackCycles)).toBe(true);
      expect(angular.isFunction(model.getFeedbackCycle)).toBe(true);
      expect(angular.isFunction(model.createFeedbackCycle)).toBe(true);
      expect(angular.isFunction(model.updateFeedbackCycle)).toBe(true);
      expect(angular.isFunction(model.createAdHocFeedback)).toBe(true);
      expect(angular.isFunction(model.getAdHocFeedbackForUser)).toBe(true);
      expect(angular.isFunction(model.getAdHocFeedbackForSelf)).toBe(true);
      expect(angular.isFunction(model.getSubmittedAdHocFeedback)).toBe(true);
  });

  describe('caches data after the first call to server', function() {

      var cacheTest = function(modelFunction, cachedService) {
          var result, httpResponse = ["dummy response array"];
          modelFunction().then(function(data) {
              result = data;
          });
          expect(cachedService).toHaveBeenCalled();
          expect(cachedService.calls.count()).toEqual(1);

          deferred.resolve({data: {body: httpResponse}});
          scope.$digest();
          expect(result).toEqual(httpResponse);

          result = undefined;
          modelFunction().then(function(data) {
              result = data;
          });
          expect(cachedService.calls.count()).toEqual(1);
          scope.$digest();
          expect(result).toEqual(httpResponse);
      }

      it('should call the account.getCurrentUser service only once', function() {
          cacheTest(model.getCurrentUser, account.getCurrentUser);
      });

      it('should call the account.getRegisteredUsers service only once', function() {
          cacheTest(model.getRegisteredUsers, account.getRegisteredUsers);
      });

      it('should call the account.getUserReports service only once', function() {
          cacheTest(model.getUserReports, account.getUserReports);
      });

      it('should call the feedback.getCycleReports service only once', function() {
          cacheTest(model.getCycleReports, feedback.getCycleReports);
      });

      it('should call the feedback.getPendingFeedbackActions service only once', function() {
          cacheTest(model.getPendingFeedbackActions, feedback.getPendingFeedbackActions);
      });

      it('should call the feedback.getCurrentFeedbackItemsForSelf service only once', function() {
          cacheTest(model.getCurrentFeedback, feedback.getCurrentFeedbackItemsForSelf);
      });

      it('should call the feedback.getFeedbackHistoryForSelf service only once', function() {
          cacheTest(model.getFeedbackHistory, feedback.getFeedbackHistoryForSelf);
      });

      it('should call the feedback.getFeedbackItem service only once', function() {
          cacheTest(model.getFeedbackDetail, feedback.getFeedbackItem);
      });

      it('should call the feedback.getActiveFeedbackCycles service only once', function() {
          cacheTest(model.getActiveFeedbackCycles, feedback.getActiveFeedbackCycles);
      });

      it('should call the feedback.getAllFeedbackCycles service only once', function() {
          cacheTest(model.getAllFeedbackCycles, feedback.getAllFeedbackCycles);
      });

      it('should call the feedback.getFeedbackCycle service only once', function() {
          cacheTest(model.getFeedbackCycle, feedback.getFeedbackCycle);
      });

      it('should call the feedback.getAdHocFeedbackForUser service only once', function() {
          cacheTest(model.getAdHocFeedbackForUser, feedback.getAdHocFeedbackForUser);
      });

      it('should call the feedback.getAdHocFeedbackForSelf service only once', function() {
          cacheTest(model.getAdHocFeedbackForSelf, feedback.getAdHocFeedbackForSelf);
      });

      it('should call the feedback.getAdHocFeedbackFromSelf service only once', function() {
          cacheTest(model.getSubmittedAdHocFeedback, feedback.getAdHocFeedbackFromSelf);
      });

      it('should call the nomination.getCurrentNominations service only once', function() {
          cacheTest(model.getCurrentNominations, nomination.getCurrentNominations);
      });

      it('should call the account.getActiveUsers service only once', function() {
          cacheTest(model.getActiveUsers, account.getActiveUsers);
      });

  });

  describe('flushes cached data when requested', function() {

      var flushTest = function(modelFunction, cachedService) {
          var httpResponse = ["dummy response array"], result;
          modelFunction().then(function(data) {
              result = data;
          });
          expect(cachedService).toHaveBeenCalled();
          expect(cachedService.calls.count()).toEqual(1);

          deferred.resolve({data: {body: httpResponse}});
          scope.$digest();
          expect(result).toEqual(httpResponse);

          result = undefined;
          modelFunction(true).then(function(data) {
              result = data;
          });
          expect(cachedService.calls.count()).toEqual(2);
          scope.$digest();
          expect(result).toEqual(httpResponse);
      }

      it('should call the account.getCurrentUser service when flushed', function(){
          flushTest(model.getCurrentUser, account.getCurrentUser);
      });

      it('should call the account.getRegisteredUsers service when flushed', function(){
          flushTest(model.getRegisteredUsers, account.getRegisteredUsers);
      });

      it('should call the account.getUserReports service when flushed', function(){
          flushTest(model.getUserReports, account.getUserReports);
      });

      it('should call the feedback.getCycleReports service when flushed', function(){
          flushTest(model.getCycleReports, feedback.getCycleReports);
      });

      it('should call the feedback.getPendingFeedbackActions service when flushed', function(){
          flushTest(model.getPendingFeedbackActions, feedback.getPendingFeedbackActions);
      });

      it('should call the feedback.getCurrentFeedbackItemsForSelf service when flushed', function(){
          flushTest(model.getCurrentFeedback, feedback.getCurrentFeedbackItemsForSelf);
      });

      it('should call the feedback.getFeedbackHistoryForSelf service when flushed', function(){
          flushTest(model.getFeedbackHistory, feedback.getFeedbackHistoryForSelf);
      });

      it('should call the feedback.getFeedbackItem service when flushed', function(){
          flushTest(model.getFeedbackDetail, feedback.getFeedbackItem);
      });

      it('should call the feedback.getActiveFeedbackCycles service when flushed', function(){
          flushTest(model.getActiveFeedbackCycles, feedback.getActiveFeedbackCycles);
      });

      it('should call the feedback.getAllFeedbackCycles service when flushed', function(){
          flushTest(model.getAllFeedbackCycles, feedback.getAllFeedbackCycles);
      });

      it('should call the feedback.getFeedbackCycle service when flushed', function(){
          flushTest(model.getFeedbackCycle, feedback.getFeedbackCycle);
      });

      it('should call the feedback.getAdHocFeedbackForSelf service when flushed', function(){
          flushTest(model.getAdHocFeedbackForSelf, feedback.getAdHocFeedbackForSelf);
      });

      it('should call the feedback.getAdHocFeedbackForUser service when flushed', function(){
          flushTest(model.getAdHocFeedbackForUser, feedback.getAdHocFeedbackForUser);
      });

      it('should call the feedback.getAdHocFeedbackFromSelf service when flushed', function(){
          flushTest(model.getSubmittedAdHocFeedback, feedback.getAdHocFeedbackFromSelf);
      });

      it('should call the nomination.getAdHocFeedbackFromSelf service when flushed', function(){
          flushTest(model.getCurrentNominations, nomination.getCurrentNominations);
      });

      it('should call the account.getActiveUsers service when flushed', function(){
          flushTest(model.getActiveUsers, account.getActiveUsers);
      });

  });

  describe('write through tests', function() {

    it('should call the account.updateCurrentUser service', function() {
      model.updateCurrentUser("1", "2");

      expect(account.updateCurrentUser).toHaveBeenCalledWith("1", "2");
    });

    it('should set the cache when the account.updateCurrentUser service is called', function() {
      model.updateCurrentUser("1", "2");

      deferred.resolve({ "data": {"body":"value"}});
      scope.$digest();

      model.getCurrentUser();
      expect(account.getCurrentUser).not.toHaveBeenCalled();
    });

    it('should call the feedback.createAdHocFeedback service', function() {
      model.createAdHocFeedback("username", "message", true);

      expect(feedback.createAdHocFeedback).toHaveBeenCalledWith("username", "message", true)
    });

    it('should update the cache when feedback.createAdHocFeedback service is called', function() {
      var result;
      model.createAdHocFeedback("username", "message", true).then(function(response){
        result = response;
      });

      deferred.resolve({ "data": {"body": "value" } });
      scope.$digest();

      expect(result).toEqual(["value"]);

      model.getSubmittedAdHocFeedback().then(function(response) {
        result = response;
      });
      scope.$digest();

      expect(result).toEqual(["value"]);
    });

    it('should update the cache when feedback.createFeedbackCycle service is called', function() {
      var result;
      model.createFeedbackCycle({"some":"thing"}).then(function(response){
        result = response;
      });

      deferred.resolve({ "data": {"body": { "id": "value" } } });
      scope.$digest();

      expect(result).toEqual({ "id": "value" });

      model.getFeedbackCycle("value").then(function(response) {
        result = response;
      });
      scope.$digest();

      expect(result).toEqual({ "id": "value" });
    });

    it('should update the cache when feedback.updateFeedbackCycle service is called', function() {
      var result;
      model.updateFeedbackCycle({"some":"thing"}).then(function(response){
        result = response;
      });

      deferred.resolve({ "data": {"body": { "id": "value" } } });
      scope.$digest();

      expect(result).toEqual({ "id": "value" });

      model.getFeedbackCycle("value").then(function(response) {
        result = response;
      });
      scope.$digest();

      expect(result).toEqual({ "id": "value" });
    });

    it('should update the cache when feedback.updateFeedback service is called', function() {
      var result;
      model.updateFeedbackDetail(123, [{"some": "thing"}, {"some":"other thing"}], undefined, undefined).then(function(response){
        result = response;
      });

      deferred.resolve({ "data": {"body": { "id": "value" } } });
      scope.$digest()

      expect(feedback.updateFeedback).toHaveBeenCalledWith(123, [{"some": "thing"}, {"some":"other thing"}], false, false)
      expect(result).toEqual({ "id": "value" })

      model.getFeedbackDetail("value").then(function(response) {
        result = response
      })
      scope.$digest()

      expect(result).toEqual({ "id": "value" })
    });
  });

  describe('flushes the entire cache', function() {

      var flushTest = function(modelFunction, cachedService) {
          var httpResponse = ["dummy response array"], result;
          modelFunction().then(function(data) {
              result = data;
          });
          expect(cachedService).toHaveBeenCalled();
          expect(cachedService.calls.count()).toEqual(1);

          deferred.resolve({data: {body: httpResponse}});
          scope.$digest();
          expect(result).toEqual(httpResponse);

          result = undefined;
          modelFunction().then(function(data) {
              result = data;
          });
          expect(cachedService.calls.count()).toEqual(1);
          scope.$digest();
          expect(result).toEqual(httpResponse);

          model.flush();

          result = undefined;
          modelFunction().then(function(data) {
              result = data;
          });
          expect(cachedService.calls.count()).toEqual(2);
          scope.$digest();
          expect(result).toEqual(httpResponse);
      }

      it('should call the account.getCurrentUser service only once', function() {
          flushTest(model.getCurrentUser, account.getCurrentUser);
      });

      it('should call the account.getRegisteredUsers service only once', function() {
          flushTest(model.getRegisteredUsers, account.getRegisteredUsers);
      });

      it('should call the account.getUserReports service only once', function() {
          flushTest(model.getUserReports, account.getUserReports);
      });

      it('should call the feedback.getCycleReports service only once', function() {
          flushTest(model.getCycleReports, feedback.getCycleReports);
      });

      it('should call the feedback.getPendingFeedbackActions service only once', function() {
          flushTest(model.getPendingFeedbackActions, feedback.getPendingFeedbackActions);
      });

      it('should call the feedback.getCurrentFeedbackItemsForSelf service only once', function() {
          flushTest(model.getCurrentFeedback, feedback.getCurrentFeedbackItemsForSelf);
      });

      it('should call the feedback.getFeedbackHistoryForSelf service only once', function() {
          flushTest(model.getFeedbackHistory, feedback.getFeedbackHistoryForSelf);
      });

      it('should call the feedback.getFeedbackItem service only once', function() {
          flushTest(model.getFeedbackDetail, feedback.getFeedbackItem);
      });

      it('should call the feedback.getActiveFeedbackCycles service only once', function() {
          flushTest(model.getActiveFeedbackCycles, feedback.getActiveFeedbackCycles);
      });

      it('should call the feedback.getAllFeedbackCycles service only once', function() {
          flushTest(model.getAllFeedbackCycles, feedback.getAllFeedbackCycles);
      });

      it('should call the feedback.getFeedbackCycle service only once', function() {
          flushTest(model.getFeedbackCycle, feedback.getFeedbackCycle);
      });

      it('should call the feedback.getAdHocFeedbackForSelf service only once', function() {
          flushTest(model.getAdHocFeedbackForSelf, feedback.getAdHocFeedbackForSelf);
      });

      it('should call the feedback.getAdHocFeedbackForUser service only once', function() {
          flushTest(model.getAdHocFeedbackForUser, feedback.getAdHocFeedbackForUser);
      });

      it('should call the feedback.getAdHocFeedbackForSelf service only once', function() {
          flushTest(model.getAdHocFeedbackForUser, feedback.getAdHocFeedbackForUser);
      });

      it('should call the nomination.getCurrentNominations service only once', function() {
          flushTest(model.getCurrentNominations, nomination.getCurrentNominations);
      });

      it('should call the account.getActiveUsers service only once', function() {
          flushTest(model.getActiveUsers, account.getActiveUsers);
      });

  });
});