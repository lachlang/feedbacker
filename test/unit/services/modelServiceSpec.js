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

		feedback = _Feedback_;
        spyOn(feedback, 'getPendingFeedbackActions').and.returnValue(deferred.promise);
        spyOn(feedback, 'getCurrentFeedbackItemsForSelf').and.returnValue(deferred.promise);
        spyOn(feedback, 'getFeedbackHistoryForSelf').and.returnValue(deferred.promise);
        spyOn(feedback, 'getFeedbackItem').and.returnValue(deferred.promise);

	}));

	it('can get an instantce of itself', function(){
    	expect(model).toBeDefined();
    });
    
    it('has defined functions', function() {
        expect(angular.isFunction(model.getCurrentUser)).toBe(true);
        expect(angular.isFunction(model.getPendingFeedbackActions)).toBe(true);
        expect(angular.isFunction(model.getCurrentFeedback)).toBe(true);
        expect(angular.isFunction(model.getFeedbackHistory)).toBe(true);
        expect(angular.isFunction(model.getFeedbackDetail)).toBe(true);
        expect(angular.isFunction(model.saveFeedback)).toBe(true);
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

    });

});