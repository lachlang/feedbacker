'use strict';

describe('edit feedback detail controller [EditCtrl]', function() {

	var scope, adminController, model, account;
	var deferred;

	beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_, _Account_) {
		scope = $rootScope.$new();

		deferred = $q.defer();

    model = _Model_;
    spyOn(model, 'getRegisteredUsers').and.returnValue(deferred.promise);
    spyOn(model, 'getAllFeedbackCycles').and.returnValue(deferred.promise);

    account = _Account_;
    spyOn(account, 'updateUser').and.returnValue(deferred.promise);

    adminController = $controller('AdminCtrl',{$scope: scope });
	}));

    describe('has valid initialisation values', function() {

    	it('should define functions', function() {
            expect(angular.isFunction(adminController.clearSelectedCycle)).toBe(true);
            expect(angular.isFunction(adminController.clearSelectedUser)).toBe(true);
            expect(angular.isFunction(adminController.getFeedbackCycle)).toBe(true);
            expect(angular.isFunction(adminController.initialiseNewCycle)).toBe(true);
            expect(angular.isFunction(adminController.updateFeedbackCycle)).toBe(true);
            expect(angular.isFunction(adminController.createNewFeedbackCycle)).toBe(true);
            expect(angular.isFunction(adminController.updateUser)).toBe(true);
            expect(angular.isFunction(adminController.removeQuestion)).toBe(true);
            expect(angular.isFunction(adminController.addQuestion)).toBe(true);
            expect(angular.isFunction(adminController.openStart)).toBe(true);
            expect(angular.isFunction(adminController.openEnd)).toBe(true);
            expect(angular.isFunction(adminController.updateQuestionResponse)).toBe(true);
    	});

    	it('for global controller variables', function() {
            expect(adminController).toBeDefined();
            expect(adminController.registeredUsers).toEqual([]);
            expect(adminController.reviewCycles).toEqual([]);
            expect(adminController.selectedUser).not.toBeDefined();
            expect(adminController.selectedCycle).not.toBeDefined();
            expect(adminController.selectedCycleDetails).not.toBeDefined();
            expect(adminController.flattenedQuestionResponse).toEqual({});
            expect(adminController.error).not.toBeDefined();
            expect(adminController.startPopup.opened).toBe(false);
            expect(adminController.endPopup.opened).toBe(false);
    	});

    	it('and calls the necessary services to pre-populate the model', function(){
            expect(model.getRegisteredUsers).toHaveBeenCalled();
            expect(model.getAllFeedbackCycles).toHaveBeenCalled();
    	});

	});

	describe('has simple state manipulation functions to', function() {

    it('open the start date picker popup', function() {
      expect(adminController.startPopup.opened).toBe(false)
      adminController.openStart();
      expect(adminController.startPopup.opened).toBe(true)
    });

    it('open the end date picker popup', function() {
      expect(adminController.endPopup.opened).toBe(false)
      adminController.openEnd();
      expect(adminController.endPopup.opened).toBe(true)
    });

    it('add a blank question to the selected cycle question array', function() {
      adminController.selectedCycleDetails = { "questions": []};
      adminController.addQuestion();
      expect(adminController.selectedCycleDetails.questions).toEqual([{ "format": "RADIO"}])
    });

    it('remove a question from the selected cycle question array', function() {
      adminController.selectedCycleDetails = { "questions" : ["zero", "one", "two", "three", "four"]}
      adminController.removeQuestion(4);
      expect(adminController.selectedCycleDetails.questions).toEqual(["zero", "one", "two", "three"]);

      adminController.removeQuestion(0);
      expect(adminController.selectedCycleDetails.questions).toEqual(["one", "two", "three"]);

      adminController.removeQuestion(1);
      expect(adminController.selectedCycleDetails.questions).toEqual(["one", "three"]);
    });

    it('clear the selected user', function() {
      adminController.selectedUser = {"some":"thing"}
      adminController.clearSelectedUser();
      expect(adminController.selectedUser).toBeUndefined()
    });

    it('clear the selected cycle', function() {
      adminController.selectedCycle = {"some":"thing"};
      adminController.selectedCycleDetails = {"some":"one"};
      adminController.flattenedQuestionResponse = {"some":"things"};
      adminController.clearSelectedCycle();

      expect(adminController.selectedCycle).toBeUndefined()
      expect(adminController.selectedCycleDetails).toBeUndefined()
      expect(adminController.flattenedQuestionResponse).toEqual({});
    });

    it('should initialise a new cycle for creation', function() {
      expect(adminController.selectedCycleDetails).toBeUndefined();
      adminController.initialiseNewCycle();
      expect(adminController.selectedCycleDetails).toEqual({
        "active": false,
        "hasForcedSharing": false,
        "hasOptionalSharing": true,
        "isThreeSixtyReview": false,
        "questions": [ {"responseOptions":[], "format": "RADIO"},
                       {"responseOptions":[], "format": "RADIO"},
                       {"responseOptions":[], "format": "RADIO"},
                       {"responseOptions":[], "format": "RADIO"},
                       {"responseOptions":[], "format": "RADIO"}]
      });
      expect(adminController.flattenedQuestionResponse).toEqual({"0":[],"1":[],"2":[],"3":[],"4":[]});
    });

    it('should update the question response options when changed', function() {
      var question = {"responseOptions": []};

      adminController.updateQuestionResponse(question, "1\n3\n5")

      expect(question).toEqual({"responseOptions":['1','3','5']});
    })
	});

});
