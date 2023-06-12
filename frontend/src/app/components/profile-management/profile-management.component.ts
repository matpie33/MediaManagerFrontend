import {Component, OnInit} from '@angular/core';
import {AbstractControl, FormGroup} from "@angular/forms";
import {FormBuilder} from "@angular/forms";
import {RestClientService} from "../../services/rest-client.service";
import {LoginConstants} from "../login/data/login-enums";
import {ViewWithStatus} from "../common/view-with-status";
import {KeyValue} from "@angular/common";

@Component({
  selector: 'app-profile-management',
  templateUrl: './profile-management.component.html',
  styleUrls: ['./profile-management.component.css']
})
export class ProfileManagementComponent extends ViewWithStatus implements OnInit{

  profileForm: FormGroup = this.formBuilder.group({
    firstName: "",
    lastName: "",
    email: "",
    phoneNumber: "",
    notificationTypes: this.formBuilder.group({
      SMS: true,
      EMAIL: true
    })
  });
  loadingData = true;

  getNotificationTypeForDisplay(notif: KeyValue<unknown, unknown>) {
    return notif.key as string;
  }

  constructor(private formBuilder:FormBuilder, private restHandler: RestClientService) {
    super();

  }

  ngOnInit(): void {
    this.restHandler.getUser(Number.parseInt(sessionStorage.getItem(LoginConstants.USER_ID)!)).subscribe(
      result => {
        this.profileForm.controls["firstName"].setValue(result.firstName);
        this.profileForm.controls["lastName"].setValue(result.lastName);
        this.profileForm.controls["email"].setValue(result.email);
        this.profileForm.controls["phoneNumber"].setValue(result.phoneNumber);
        let smsControl:AbstractControl = this.profileForm.get("notificationTypes")!.get("SMS")!;
        smsControl.setValue(result.acceptedNotificationTypes.includes("SMS"));
        let emailControl:AbstractControl = this.profileForm.get("notificationTypes")!.get("EMAIL")!;
        emailControl.setValue(result.acceptedNotificationTypes.includes("EMAIL"));
        this.loadingData = false;
      }
    );
    }

    private getSelectedNotifications() {
        let controls = this.profileForm.get("notificationTypes")!.value;
        let selectedNotifications = [];
        for (let controlName in controls){
          if (controls[controlName] == true){
            selectedNotifications.push(controlName);
          }
        }
        return selectedNotifications;
      }

  onSubmitProfile() {
    let profileData = {
      firstName: this.profileForm.controls["firstName"].value,
      lastName: this.profileForm.controls["lastName"].value,
      email: this.profileForm.controls["email"].value,
      phoneNumber: this.profileForm.controls["phoneNumber"].value,
      acceptedNotificationTypes: this.getSelectedNotifications()
    }
    this.showInfoMessage("Saving data...");
    this.restHandler.editUser(profileData, Number.parseInt(sessionStorage.getItem(LoginConstants.USER_ID)!)).subscribe({
      next: this.handleEditPersonalDataDone.bind(this)
    });
    this.profileForm.markAsPristine();
  }

  handleEditPersonalDataDone (){
    this.showSuccessMessage("Data has been successfully saved!");
    this.hideStatusAfterDelay();
  }

}
