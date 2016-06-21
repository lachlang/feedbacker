'use strict';

describe('reports controller [ReportsCtrl]', function() {

	var scope, reportsController, model, nomination;
	var deferred, deferredNom;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, _Nomination_) {

		deferred = $q.defer();
		deferredNom = $q.defer();

    	model = _Model_;
        spyOn(model, 'getReports').and.returnValue(deferred.promise);

		reportsController = $controller('ReportsCtrl',{});
	}));

    describe('has valid initialisation values', function() {

//    	it('should define functions', function() {
//            expect(angular.isFunction(nominationController.addNomination)).toBe(true);
//            expect(angular.isFunction(nominationController.cancelNomination)).toBe(true);
//    	});
//
//    	it('for global controller variables', function() {
//            expect(nominationController).toBeDefined();
//            expect(nominationController.nominations).toEqual([]);
//            expect(nominationController.nomineeCandidates).toEqual([]);
//            expect(nominationController.nominee).toBeUndefined();
//    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getReports).toHaveBeenCalled();
//            expect(model.getNomineeCandidates).toHaveBeenCalled();
//            expect(model.getActiveFeedbackCycles).toHaveBeenCalled();
    	});

	});

});
