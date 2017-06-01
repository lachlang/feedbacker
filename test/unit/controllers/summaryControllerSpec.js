'use strict';

describe('summary controller [SummaryCtrl]', function() {

  var scope, summaryController, model;
  var deferredUser, deferredCurrent, deferredHistory, deferredAdHoc;

  beforeEach(module('feedbacker'));

    // define the mock person and relationship services
    beforeEach(inject(function($rootScope, $q, $controller, _Model_) {
    scope = $rootScope.$new();

    deferredUser = $q.defer();
    deferredCurrent = $q.defer();
    deferredHistory = $q.defer();
    deferredAdHoc = $q.defer();

    model = _Model_;
    spyOn(model, 'getCurrentUser').and.returnValue(deferredUser.promise);
    spyOn(model, 'getCurrentFeedback').and.returnValue(deferredCurrent.promise);
    spyOn(model, 'getFeedbackHistory').and.returnValue(deferredHistory.promise);
    spyOn(model, 'getAdHocFeedbackForSelf').and.returnValue(deferredAdHoc.promise);

    summaryController = $controller('SummaryCtrl',{$scope: scope });
  }));

  describe('has valid initialisation values', function() {

    it('for global controller variables', function() {
      expect(summaryController).toBeDefined();
      expect(summaryController.user).toEqual({});
      expect(summaryController.currentFeedbackList).toEqual([]);
      expect(summaryController.feedbackHistoryList).toEqual([]);
      expect(summaryController.adHocFeedbackList).toEqual([]);
    });

    it('and calls the necessary services to pre-populate the model', function(){
      expect(model.getCurrentUser).toHaveBeenCalled();
      expect(model.getCurrentFeedback).toHaveBeenCalled();
      expect(model.getFeedbackHistory).toHaveBeenCalled();
      expect(model.getAdHocFeedbackForSelf).toHaveBeenCalled();

      deferredUser.resolve("user");
      deferredCurrent.resolve("current");
      deferredHistory.resolve("history");
      deferredAdHoc.resolve("adHoc");
      scope.$digest();

      expect(summaryController.user).toEqual("user");
      expect(summaryController.currentFeedbackList).toEqual("current");
      expect(summaryController.feedbackHistoryList).toEqual("history");
      expect(summaryController.adHocFeedbackList).toEqual("adHoc");
    });

  });

});
