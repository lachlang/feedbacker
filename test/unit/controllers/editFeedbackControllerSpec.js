'use strict';

describe('edit feedback detail controller [EditCtrl]', function() {

	var scope, editController, model, location;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, $location) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getFeedbackDetail').and.returnValue(deferred.promise);

        location = $location;
        spyOn(location, "search").and.returnValue({"id":12});

		editController = $controller('EditCtrl',{$scope: scope });
        spyOn(editController, 'initialiseController').and.callThrough();
	}));

    describe('has valid intiatialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(editController.initialiseController)).toBe(true);
            expect(angular.isFunction(editController.save)).toBe(true);
            expect(angular.isFunction(editController.cancel)).toBe(true);
            expect(angular.isFunction(editController.navigateToList)).toBe(true);
            expect(angular.isFunction(editController.resetError)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(editController).toBeDefined();
            expect(editController.questions).toEqual([]);
            expect(editController.error).not.toBeDefined();
            expect(editController.feedbackForName).not.toBeDefined();
            expect(editController.managerName).not.toBeDefined();
            expect(editController.shareFeedback).toBe(false);
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getFeedbackDetail).toHaveBeenCalled();
    	});

	});

});
