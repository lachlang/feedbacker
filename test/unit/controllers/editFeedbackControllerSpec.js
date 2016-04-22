'use strict';

describe('edit feedback detail controller [EditCtrl]', function() {

	var scope, editController, model;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getFeedbackDetail').and.returnValue(deferred.promise);
        // spyOn(model, 'getCurrentFeedback').and.returnValue(deferred.promise);

		editController = $controller('EditCtrl',{$scope: scope });
	}));

    describe('has valid intiatialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(editController.initialiseController)).toBe(true);
            expect(angular.isFunction(editController.save)).toBe(true);
            expect(angular.isFunction(editController.cancel)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(editController).toBeDefined();
            expect(editController.questions).toBeDefined();
            expect(editController.feedbackId).not.toBeDefined();
    	});

    	// it('and calls the necessary services to pre-populate the model', function(){
     //        expect(model.getPendingFeedbackActions).toHaveBeenCalled();
     //        expect(model.getCurrentFeedback).toHaveBeenCalled();
     //        expect(model.getFeedbackHistory).toHaveBeenCalled();
    	// });

	});

});
