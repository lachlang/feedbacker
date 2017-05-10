'use strict';

describe('summary controller [SummaryCtrl]', function() {

	var scope, summaryController, model;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getCurrentUser').and.returnValue(deferred.promise);
        spyOn(model, 'getCurrentFeedback').and.returnValue(deferred.promise);
        spyOn(model, 'getFeedbackHistory').and.returnValue(deferred.promise);

		summaryController = $controller('SummaryCtrl',{$scope: scope });
	}));

    describe('has valid initialisation values', function() {

    	it('should define functions', function() {
            // TODO: add functions here if they get added to the controller
    	});

    	it('for global controller variables', function() {
            expect(summaryController).toBeDefined();
            expect(summaryController.currentFeedbackList).toBeDefined();
            expect(summaryController.feedbackHistoryList).toBeDefined();
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getCurrentUser).toHaveBeenCalled();
            expect(model.getCurrentFeedback).toHaveBeenCalled();
            expect(model.getFeedbackHistory).toHaveBeenCalled();
    	});

	});

});
