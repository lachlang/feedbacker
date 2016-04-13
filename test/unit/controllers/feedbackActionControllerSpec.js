'use strict';

describe('feedback action controller [FeedbackActionCtrl]', function() {

	var scope, feedbackActionController, model;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getPendingFeedbackActions').and.returnValue(deferred.promise);
        spyOn(model, 'getCurrentFeedback').and.returnValue(deferred.promise);
        spyOn(model, 'getFeedbackHistory').and.returnValue(deferred.promise);

		feedbackActionController = $controller('FeedbackActionCtrl',{$scope: scope });
	}));

    describe('has valid intiatialisation values', function() {

    	it('should define functions', function() {
    		expect(angular.isFunction(feedbackActionController.viewFeedbackDetail)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(feedbackActionController).toBeDefined();
            expect(feedbackActionController.actions).toBeDefined();
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getPendingFeedbackActions).toHaveBeenCalled();
            expect(model.getCurrentFeedback).toHaveBeenCalled();
            expect(model.getFeedbackHistory).toHaveBeenCalled();
    	});

	});

});
