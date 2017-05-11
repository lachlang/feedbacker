'use strict';

describe('service [Model]', function() {
	
	var account, feedback, model;
	var deferred, scope;
	
	beforeEach(module('feedbacker.services'));

	beforeEach(inject(function($q, _Model_, _Account_, _Feedback_, $rootScope) {
		scope = $rootScope.$new();
		deferred = $q.defer();
		model = _Model_;

        account = _Account_;
        spyOn(account, 'getCurrentUser').and.returnValue(deferred.promise)
        spyOn(account, 'updateCurrentUser').and.returnValue(deferred.promise)
        spyOn(account, 'getReports').and.returnValue(deferred.promise)

		feedback = _Feedback_;
        spyOn(feedback, 'getPendingFeedbackActions').and.returnValue(deferred.promise);
        spyOn(feedback, 'getCurrentFeedbackItemsForSelf').and.returnValue(deferred.promise);
        spyOn(feedback, 'getFeedbackHistoryForSelf').and.returnValue(deferred.promise);
        spyOn(feedback, 'getFeedbackItem').and.returnValue(deferred.promise);
        spyOn(feedback, 'getActiveFeedbackCycles').and.returnValue(deferred.promise);
        spyOn(feedback, 'getFeedbackCycle').and.returnValue(deferred.promise);
        spyOn(feedback, 'createAdHocFeedback').and.returnValue(deferred.promise);
        spyOn(feedback, 'getAdHocFeedbackFor').and.returnValue(deferred.promise);
        spyOn(feedback, 'getAdHocFeedbackFrom').and.returnValue(deferred.promise);

	}));

	it('can get an instantce of itself', function(){
    	expect(model).toBeDefined();
    });
    
    it('has defined functions', function() {
        expect(angular.isFunction(model.flush)).toBe(true);
        expect(angular.isFunction(model.getCurrentUser)).toBe(true);
        expect(angular.isFunction(model.getReports)).toBe(true);
        expect(angular.isFunction(model.getPendingFeedbackActions)).toBe(true);
        expect(angular.isFunction(model.updateCurrentUser)).toBe(true);
        expect(angular.isFunction(model.getCurrentFeedback)).toBe(true);
        expect(angular.isFunction(model.getFeedbackHistory)).toBe(true);
        expect(angular.isFunction(model.getFeedbackDetail)).toBe(true);
        expect(angular.isFunction(model.getNomineeCandidates)).toBe(true);
        expect(angular.isFunction(model.getCurrentNominations)).toBe(true);
        expect(angular.isFunction(model.getActiveFeedbackCycles)).toBe(true);
        expect(angular.isFunction(model.getFeedbackCycle)).toBe(true);
        expect(angular.isFunction(model.createAdHocFeedback)).toBe(true);
        expect(angular.isFunction(model.getAdHocFeedbackFor)).toBe(true);
        expect(angular.isFunction(model.getAdHocFeedbackFrom)).toBe(true);
    });

    describe('caches data after the first call to server', function() {
    	
        var cacheTest = function(modelFunction, cachedService) {
            var result, httpResponse = "dummy response";
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

        it('should call the account.getReports service only once', function() {
            cacheTest(model.getReports, account.getReports);
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

        it('should call the feedback.getFeedbackCycle service only once', function() {
            cacheTest(model.getFeedbackCycle, feedback.getFeedbackCycle);
        });

        it('should call the feedback.getAdHocFeedbackFor service only once', function() {
            cacheTest(model.getAdHocFeedbackFor, feedback.getAdHocFeedbackFor);
        });

        it('should call the feedback.getAdHocFeedbackFrom service only once', function() {
            cacheTest(model.getAdHocFeedbackFrom, feedback.getAdHocFeedbackFrom);
        });

    });
    
    describe('flushes cached data when requested', function() {

        var flushTest = function(modelFunction, cachedService) {
            var httpResponse = "dummy response", result;
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

        it('should call the account.getReport service when flushed', function(){
            flushTest(model.getReports, account.getReports);
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

        it('should call the feedback.getFeedbackCycle service when flushed', function(){
            flushTest(model.getFeedbackCycle, feedback.getFeedbackCycle);
        });

        it('should call the feedback.getAdHocFeedbackFor service when flushed', function(){
            flushTest(model.getAdHocFeedbackFor, feedback.getAdHocFeedbackFor);
        });

        it('should call the feedback.getAdHocFeedbackFrom service when flushed', function(){
            flushTest(model.getAdHocFeedbackFrom, feedback.getAdHocFeedbackFrom);
        });
    });

    describe('write through tests', function() {

        it('should call the account.updateCurrentUser service', function() {
            model.updateCurrentUser("1", "2","3");

            expect(account.updateCurrentUser).toHaveBeenCalledWith("1", "2", "3");
        });

        it('should set the cache when the account.updateCurrentUser service is called', function() {
            model.updateCurrentUser("1", "2","3");

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

          model.getAdHocFeedbackFrom().then(function(response) {
            result = response;
          });
          scope.$digest();

          expect(result).toEqual(["value"]);
        });
    });

    describe('flushes the entire cache', function() {

        var flushTest = function(modelFunction, cachedService) {
            var httpResponse = "dummy response", result;
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

        it('should call the account.getReports service only once', function() {
            flushTest(model.getReports, account.getReports);
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

        it('should call the feedback.getFeedbackCycle service only once', function() {
            flushTest(model.getFeedbackCycle, feedback.getFeedbackCycle);
        });


    });
});