'use strict';

describe('reports controller [ReportsCtrl]', function() {

	var scope, reportsController, model;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, _Nomination_) {

		deferred = $q.defer();

    	model = _Model_;
        spyOn(model, 'getReports').and.returnValue(deferred.promise);

		reportsController = $controller('ReportsCtrl',{});
	}));

    describe('has valid initialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(reportsController.export)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(reportsController.displayFilter).toBeDefined();
            expect(reportsController.displayFilter).toEqual('current');
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getReports).toHaveBeenCalled();
    	});

	});

});
