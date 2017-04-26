package se.chalmers.eda397.team9.cardsagainsthumanity.Classes;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Created by Mikae on 2017-04-21.
 */

public class Game implements Serializable {
    private ArrayList<Player> players;
    private Player king;
    private BlackCard blackCard;
    private ArrayList<CardExpansion> cardExpansions;
    private Random r;
    private ArrayList<WhiteCard> submittedWhiteCards;

    public Game(ArrayList<Player> players, ArrayList<CardExpansion> cardExpansions) {
        r = new Random();
        this.players = players;
        this.cardExpansions = cardExpansions;
        //king = setKing();
        pickBlackCard();
        distributeWhiteCards();
    }
    //Call this method with what ever update frequency you want
    public void update(){
        giveCardsToKing();
    }
    //gives each player 10 cards from selected expansion
    private void distributeWhiteCards() {
        for (Player p : players) {
            if (!p.isKing()) {
                while(p.getWhiteCards().size() < 10) {
                    CardExpansion exp = cardExpansions.get(r.nextInt(cardExpansions.size()));
                    WhiteCard whiteCard = exp.getWhiteCards().get(r.nextInt(exp.getWhiteCards().size()));
                    p.addWhiteCard(whiteCard);
                }
            }
        }
    }
    //Selects a random black card from selected expansioons
    private void pickBlackCard() {
        CardExpansion exp = cardExpansions.get(r.nextInt(cardExpansions.size()));
        blackCard = exp.getBlackCards().get(r.nextInt(exp.getBlackCards().size()));
        for (Player p : players) {
            p.setBlackCard(blackCard);
        }
    }
    private Player setKing() {
        for (Player p : players) {
            p.setKing(false);
        }
        int kingNumber = r.nextInt(players.size());
        players.get(kingNumber).setKing(true);
        return players.get(kingNumber);
    }
    
    //collects all submitted white cards and gives them to the king
    private void giveCardsToKing() {
        List<Submission> sub = new ArrayList<Submission>();
        king.resetSubmissions();
        for (Player p : players) {
            if(!p.equals(king)) {
                sub.add(p.getSubmission());
            }
        }
        king.setSubmissions(sub);
    }
    public Boolean hasAllPlayersSubmitted(){
        return (submittedWhiteCards.size() == (players.size()-1) * blackCard.getPick());
    }

    public Player getPlayerByUserName(String name) {
        for (Player p : players) {
            if (p.getUsername().equals(name)) {
                return p;
            }
        }
        return null;
    }

    public BlackCard getBlackCard() {
        return blackCard;
    }
}
