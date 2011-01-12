package
{
  import flash.events.MouseEvent;
  import flash.external.ExternalInterface;
  
  import mx.controls.LinkButton;

  public class ExternalLinkButton extends LinkButton
  {
    public var resourceId:String = "";
    
    public function ExternalLinkButton()
    {
      super();
      addEventListener(MouseEvent.CLICK, followLink);
    }
    
    private function followLink(event:MouseEvent):void
    {
      ExternalInterface.call("goToResource",resourceId);
    }
  }
}