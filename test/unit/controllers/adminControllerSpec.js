'use strict';

describe('edit feedback detail controller [EditCtrl]', function() {

	var scope, adminController, model, location;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getRegisteredUsers').and.returnValue(deferred.promise);
        spyOn(model, 'getAllFeedbackCycles').and.returnValue(deferred.promise);

    		adminController = $controller('AdminCtrl',{$scope: scope });
	}));

    describe('has valid initialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(adminController.clearSelectedCycle)).toBe(true);
            expect(angular.isFunction(adminController.createNewCycle)).toBe(true);
            expect(angular.isFunction(adminController.updateFeedbackCycle)).toBe(true);
            expect(angular.isFunction(adminController.createFeedbackCycle)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(adminController).toBeDefined();
            expect(adminController.registeredUsers).toEqual([]);
            expect(adminController.reviewCycles).toEqual([]);
            expect(adminController.selected).not.toBeDefined();
            expect(adminController.error).not.toBeDefined();
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getRegisteredUsers).toHaveBeenCalled();
            expect(model.getAllFeedbackCycles).toHaveBeenCalled();
    	});

	});

});
